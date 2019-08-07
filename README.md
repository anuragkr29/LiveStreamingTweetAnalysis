# LiveStreamingTweetAnalysis
A Spark Streaming application that will continuously read data from Twitter about a topic. These Twitter feeds will be analyzed for their sentiment, and then analyzed using ElasticSearch. To exchange data between these two, you will use Kafka as a broker. Everything will be done locally on your computer
Setting up your development environment
You will need to perform the following steps to get your environment set up.
• You will need to create a Twitter app and get credentials. It can be done at
     https://apps.twitter.com/
• Download Apache Kafka and go through the quickstart steps:
     https://kafka.apache.org/quickstart
• Windows users might want to consider WSL, which gives a Unix-like environment on Windows:
https://docs.microsoft.com/en-us/windows/wsl/install-win10
• You will also need to set up Elasticsearch and Kibana environment to visualize data. You will need to download Elasticsearch, Kibana, and Logstash from https://www.elastic.co/downloads/

Starting the Project
For this project, you will need to perform the following steps:
• Create a Twitter application using Spark and Scala. An example of how to do this is available here:
http://bahir.apache.org/docs/spark/current/spark-streaming-twitter/
• The application should perform search about a topic and gather Tweets related to it. The next step is sentiment evaluation of the Tweets. Various libraries are available for this task:
– CoreNLP Scala Examples
– Sentiment Analyis in Scala
– Sentiment Analysis of Social Media Posts using Spark
The sentiment evaluation should happen continuously using a stream approach. At the end of every window, a message containing the sentiment should be sent to Kafka through a topic.
• In the next step, you will configure Logstash, Elasticsearch, and Kibana to read from the topic and set up visualization of sentiment.

## Steps for visualization using Kafka and Kibana
+ To send data from Kafka to Elasticsearch, you need a processing pipeline that is provided by Logstash. Download Elasticsearch, Kibana, and Logstash from https://www.elastic.co/downloads.
+ After downloading the data, you need to go to the appropriate directories, and start the services in the following order:
    1. Elasticsearch using the following command in the elasticsearch-***/bin directory: ./elasticsearch
    2. Kibana using the following command in the kibana-***/bin directory:
    ./kibana
    3. Logstash: Go to the logstash-*** directory and create a file logstash-simple.conf with following content:
         
         input {
             kafka {
             bootstrap_servers => "localhost:9092"
             topics => ["YourTopic"]
             }
         }
         output {
             elasticsearch {
             hosts => ["localhost:9200"]
             index => "YourTopic-index"
             }
         }
    4. Then run the following command
         + bin/logstash -f logstash-simple.conf
+ This sets up the right pipeline between Kafka and Elasticsearch.
+ If everything is set up properly, you should be able to go to http://localhost:5601 and use Kibana to visualize your data in real-time. You will have to search for the appropriate topic index, which is YourTopic-index in the example shown above.
### Note: 
- If you have trouble setting up all of this on your local computer, please use online sources for help with troubleshooting.
