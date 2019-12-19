package FlinkKafka;
import java.util.Properties;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.*;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
/* parser imports */
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaDemo {

    static Integer globalCounter=0;
    public static void main(String[] args) throws Exception
    {
        final StreamExecutionEnvironment env =  StreamExecutionEnvironment.getExecutionEnvironment();

        Properties p = new Properties();
        p.setProperty("bootstrap.servers", "127.0.0.1:9092");
        p.setProperty("zookeeper.connect", "localhost:2181");
        p.setProperty("group.id", "test");

        DataStream<String> kafkaData = env.addSource(new FlinkKafkaConsumer011("test", new SimpleStringSchema(), p));

        //##########Going to perform 3 Tuple operation to see how the DAG looks like. ###########


        /*#####################

        kafkaData.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>()
        {
            public void flatMap(String value, Collector<Tuple2<String, Integer>> out)
            {
                String[] words = value.split(" ");
                for (String word : words)
                    out.collect(new Tuple2<String, Integer>(word, 1));
            }	})

                .keyBy(0)
                .sum(1)
        .writeAsText("/Users/mohammedabdulrazzak/Documents/ZignalLabs/Technology/ApacheFlick//kafka.txt");
*/

        DataStream<Tuple2<String, Integer>> kf2 = kafkaData.flatMap(new TweetParser());

        kf2.writeAsText("/Users/mohammedabdulrazzak/Documents/ZignalLabs/Technology/ApacheFlick//kafka.txt");

        //kf2.addSink(new FlinkKafkaProducer09("flinkSink", new SimpleStringSchema(), p));

        env.execute("Kafka Example");
    }

    public static class TweetParser	implements FlatMapFunction<String, Tuple2<String, Integer>>
    {

        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception
        {
            ObjectMapper jsonParser = new ObjectMapper();
            JsonNode node = jsonParser.readValue(value, JsonNode.class);

            boolean isEnglish =
                    node.has("user") &&
                            node.get("user").has("lang") &&
                            node.get("user").get("lang").asText().equals("en");

            boolean hasText = node.has("body");

            String tweet = "No Text  ???  ";
            if (hasText)
            {
                 tweet = node.get("body").asText();
            } else {
                tweet = node.get ("text").asText();
            }
            tweet = tweet + " ===  Tweete Count: ";
            out.collect(new Tuple2<String, Integer>(tweet, globalCounter++));
        }
    }


}