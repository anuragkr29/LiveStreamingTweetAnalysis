import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage._
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}



object KafkaExample {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("kafka-tutorials").setMaster("local[4]")
      .set("spark.driver.host", "localhost")
    if (args.length < 5) {
      println("Correct usage: Program_Name outputTopic TwitterconsumerKey consumerSecret accessToken accessTokenSecret")
      System.exit(1)
    }

    val inTopic = args(0)

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.WARN)


    val consumerKey = args(1)
    val consumerSecret = args(2)
    val accessToken = args(3)
    val accessTokenSecret = args(4)
    val filters = Seq("chandrayaan2", "Trump", "ps4", "POTUS", "Weather", "Apple")
    // Set the system properties so that Twitter4j library used by Twitter stream
    // can use them to generate OAuth credentials
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    // Now let's wrap the context in a streaming one, passing along the window size
    @transient val ssc = new StreamingContext(sparkConf, Seconds(2))
    val sqlContext = new SQLContext(ssc.sparkContext)
    // Creating a stream from Twitter
    val tweets = TwitterUtils.createStream(ssc, None, filters, StorageLevel.MEMORY_ONLY_SER_2)
    // To compute the sentiment of a tweet we'll use different set of words used to
    // filter and score each word of a sentence. Since these lists are pretty small
    // it can be worthwhile to broadcast those across the cluster so that every
    // executor can access them locally
    val englishTweets = tweets.filter(_.getLang() == "en")
    val statuses = englishTweets.map(status => (status.getText()))

    statuses.foreachRDD { (rdd, time) =>
      rdd.foreachPartition { partitionIter =>
        val props = new Properties()
        val bootstrap = "localhost:9092" //-- your external ip of GCP VM, example: 10.0.0.1:9092
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("bootstrap.servers", bootstrap)
        val producer = new KafkaProducer[String, String](props)

        partitionIter.foreach { elem =>
          val dat = elem.toString()
          val sentiment = SentimentAnalyzer.mainSentiment(dat).toString()
          val data = new ProducerRecord[String, String](inTopic, "sentiment", sentiment + "->" +  dat)
          producer.send(data)
        }
        producer.flush()
        producer.close()
      }
    }
    ssc.start()
    ssc.awaitTermination()
  }
}
