package com.socialmediaraiser.twitterbot.impl.personalAnalyzer;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInteractions {

  List<UserInteractionX> values = new ArrayList<>();

  public UserInteractionX get(String userId) {
    for (UserInteractionX userInteraction : values) {
      if (userInteraction.getUserId().equals(userId)) {
        return userInteraction;
      }
    }
    UserInteractionX userInteraction = new UserInteractionX(userId);
    this.values.add(userInteraction);
    return userInteraction;
  }

  @Getter
  @Setter
  public static class UserInteractionX {

    private String userId;
    private int    nbRepliesGiven     = 0;
    private int    nbRepliesReceived  = 0;
    private int    nbRetweetsReceived = 0;
    private int    nbRetweetsGiven    = 0;
    private int    nbLikesGiven       = 0;

    public UserInteractionX(String userId) {
      this.userId = userId;
    }

    public void incrementNbRepliesGiven() {
      nbRepliesGiven++;
    }

    public void incrementNbRepliesReceived() {
      nbRepliesReceived++;
    }

    public void incrementNbLikesGiven() {
      nbLikesGiven++;
    }

    public void incrementNbRetweetsReceived() {
      nbRetweetsReceived++;
    }

    public void incrementNbRetweetsGiven() {
      nbRetweetsGiven++;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
      UserInteractionX other = (UserInteractionX) o;
      return other.getUserId().equals(this.userId);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 17 * hash + (this.userId != null ? this.userId.hashCode() : 0);
      return hash;
    }
  }


}
