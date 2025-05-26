package dev.nextchat.server.messaging.repository;

import java.util.List;
import dev.nextchat.server.messaging.model.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class CustomMessageRepositoryImpl implements CustomMessageRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Message> findLatestMessagesPerGroupBeforeTimestamp(List<UUID> groupIds, Instant beforeTime) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("groupId").in(groupIds)
                .and("timestamp").lt(beforeTime));

        SortOperation sortStage = Aggregation.sort(Sort.by(Sort.Order.desc("timestamp"), Sort.Order.asc("groupId")));

        GroupOperation groupStage = Aggregation.group("groupId")
                .first(Aggregation.ROOT).as("latestMessage");

        ReplaceRootOperation replaceRootStage = Aggregation.replaceRoot("latestMessage");

        LimitOperation limitStage = Aggregation.limit(10);

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage, sortStage, groupStage, replaceRootStage, limitStage);

        return mongoTemplate.aggregate(aggregation, "messages", Message.class).getMappedResults();
    }
}
