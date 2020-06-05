package com.socialmediaraiser.twitterbot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.socialmediaraiser.twitter.TwitterClient;
import com.socialmediaraiser.twitterbot.properties.GoogleCredentials;
import com.socialmediaraiser.twitterbot.properties.IOProperties;
import com.socialmediaraiser.twitterbot.properties.InfluencerProperties;
import com.socialmediaraiser.twitterbot.properties.TargetProperties;
import com.socialmediaraiser.twitterbot.scoring.ScoringProperty;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.CustomLog;
import lombok.Data;
import lombok.Getter;


@Data
@CustomLog
public class FollowProperties {

  @Getter
  private static TargetProperties     targetProperties;
  @Getter
  private static ScoringProperties    scoringProperties;
  @Getter
  private static InfluencerProperties influencerProperties;
  @Getter
  private static IOProperties         ioProperties;
  @Getter
  private static GoogleCredentials    googleCredentials;
  @Getter
  private static String               arraySeparator = ",";

  private FollowProperties() {
    throw new IllegalStateException("Utility class");
  }

  public static boolean load(String userName) {
    if (userName == null) {
      return false;
    }
    try {
      URL yamlFile = com.socialmediaraiser.twitterbot.FollowProperties.class.getResource("/" + userName + ".yaml");
      if (yamlFile == null) {
        yamlFile = com.socialmediaraiser.twitterbot.FollowProperties.class.getResource("/RedTheOne.yaml"); // @todo to clean
      }
      //URL yamlFile = new File("/"+userName+".yaml").toURI().toURL();
      if (yamlFile == null) {
        LOGGER.severe(() -> "yaml file not found at /" + userName + ".yaml");
        return false;
      }
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      TwitterClient.OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      Map<String, Object> yaml        = mapper.readValue(yamlFile, HashMap.class);
      Map<String, Object> scoringList = (Map<String, Object>) yaml.get("scoring");
      if (scoringList != null) {

        List<ScoringProperty> scoringPropertyList = new ArrayList<>();
        for (Map.Entry<String, Object> p : scoringList.entrySet()) {
          ScoringProperty sp = TwitterClient.OBJECT_MAPPER.convertValue(p.getValue(), ScoringProperty.class);
          sp.setCriterion(p.getKey());
          scoringPropertyList.add(sp);
        }
        scoringProperties = new ScoringProperties(scoringPropertyList);
      }
      targetProperties     = TwitterClient.OBJECT_MAPPER.convertValue(yaml.get("target"), TargetProperties.class);
      googleCredentials    = TwitterClient.OBJECT_MAPPER.convertValue(yaml.get("google-credentials"), GoogleCredentials.class);
      influencerProperties = TwitterClient.OBJECT_MAPPER.convertValue(yaml.get("influencer"), InfluencerProperties.class);
      ioProperties         = TwitterClient.OBJECT_MAPPER.convertValue(yaml.get("io"), IOProperties.class);
      LOGGER.info(() -> "properties loaded correctly");
      return true;
    } catch (IOException ex) {
      LOGGER.severe(() -> "properties could not be loaded (" + ex.getMessage() + ")");
      return false;
    }
  }

}
