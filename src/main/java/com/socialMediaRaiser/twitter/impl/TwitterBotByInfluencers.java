package com.socialMediaRaiser.twitter.impl;

import com.socialMediaRaiser.twitter.AbstractTwitterBot;
import com.socialMediaRaiser.twitter.FollowProperties;
import com.socialMediaRaiser.twitter.User;
import com.socialMediaRaiser.twitter.helpers.IOHelper;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class TwitterBotByInfluencers extends AbstractTwitterBot {

    private List<User> potentialFollowers = new ArrayList<>();
    private int maxFriendship = 390;

    @Override
    public List<User> getPotentialFollowers(Long ownerId, int count, boolean follow, boolean saveResults){
        if(count>maxFriendship) count = maxFriendship;
        int minOccurence = 0;
        List<User> ownerFollowers = this.getFollowerUsers(ownerId);
        List<User> influencers = this.getInfluencersFromUsers(ownerFollowers, 150);
        Collections.shuffle(influencers);

        Map<Long, Long> sortedPotentialFollowersMap =
                this.getAllFollowerIdsFromUsersSortedByOccurence(ownerId, influencers,
                        FollowProperties.targetProperties.getNbBaseFollowers(), minOccurence);

        Iterator<Map.Entry<Long, Long>> it = sortedPotentialFollowersMap.entrySet().iterator();
        int iteration = 0;
        long startWorkingTime = System.currentTimeMillis();
        long stopWorkingTime;
        while (it.hasNext() && potentialFollowers.size() < count) {

            if(iteration%50==0 && iteration>0){
                stopWorkingTime = System.currentTimeMillis();
                System.out.println("i: " + (int)(iteration*100/(float)sortedPotentialFollowersMap.size()) + "% "
                        + "F: " + (float)potentialFollowers.size()*100/(float)count + "% "
                        + "in "+ (stopWorkingTime-startWorkingTime)/(float)1000 + "s ");
                startWorkingTime = System.currentTimeMillis();
            }

            Map.Entry<Long, Long> entry = it.next();
            if(entry.getKey()!=null && entry.getValue()!=null){
                Long userId = entry.getKey();
                User potentialFollower = this.getUserFromUserId(userId); // criticity here (900/15min)
                if(potentialFollower!=null){
                    potentialFollower.setCommonFollowers(Math.toIntExact(entry.getValue()));
                    if (potentialFollower.shouldBeFollowed()) {
                        if(!FollowProperties.ioProperties.isUseRFA() || potentialFollower.getRandomForestPrediction()) {
                            if (this.isLanguageOK(potentialFollower)) {
                                if (follow) {
                                    boolean result = this.follow(potentialFollower.getId());
                                    if (result) {
                                        potentialFollower.setDateOfFollowNow();
                                        potentialFollowers.add(potentialFollower);
                                        if (saveResults) {
                                            this.getIOHelper().addNewFollowerLine(potentialFollower);
                                        }
                                    }
                                } else {
                                    System.out.println("potentialFollowers added : " + potentialFollower.getUserName());
                                    potentialFollowers.add(potentialFollower);
                                }
                            }
                        }
                    }
                }
            }
            iteration++;
        }
        System.out.println("********************************");
        System.out.println(potentialFollowers.size() + " followers followed / "
                + iteration + " users analyzed");
        System.out.println("********************************");

        return potentialFollowers;
    }

    private List<User> getInfluencersFromUsers(List<User> users, int count){
        List<User> followersInfluencers = new ArrayList<>();
        User user;
        int i=0;
        // building influencers list
        while(i< users.size() && followersInfluencers.size() < count){
            user = users.get(i);
            if(user.isInfluencer() && this.isLanguageOK(user)){
                    followersInfluencers.add(user);
            }
            i++;
        }

        return followersInfluencers;
    }


    // id, occurencies
    private Map<Long, Long> getAllFollowerIdsFromUsersSortedByOccurence(Long ownerId, List<User> followers, int nbFollowersMaxtoWatch, int minOccurence){
        List<Long> ownerFollowingIds = this.getFollowingIds(ownerId);
        ownerFollowingIds.add(ownerId);
        List<Long> followedRecently = this.getIOHelper().getPreviouslyFollowedIds();
        // building influencers followers list
        List<Long> influencersFollowersIds = new ArrayList<>();
        User user;
        int i=0;
        while(i<followers.size() && i<nbFollowersMaxtoWatch){
            user = followers.get(i);
            List<Long> currentFollowersInfluencersFollowersId = this.getFollowerIds(user.getId()); // criticity here -> cache
            //  influencersFollowersIds.addAll(currentFollowersInfluencersFollowersId);
            for(Long userId : currentFollowersInfluencersFollowersId){
                if(ownerFollowingIds.indexOf(userId)==-1 && followedRecently.indexOf(userId)==-1) {
                    influencersFollowersIds.add(userId);
                }
            }
            System.out.println(user.getUserName() + " (" + currentFollowersInfluencersFollowersId.size() + " followers)");
            i++;
        }
        Map<Long, Long> sortedPotentialFollowersMap = influencersFollowersIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))// create a map with item, occurence
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .filter(x -> x.getValue()>=minOccurence)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        System.out.println(sortedPotentialFollowersMap.size() + " followers found \n");
        return sortedPotentialFollowersMap;
    }
}
