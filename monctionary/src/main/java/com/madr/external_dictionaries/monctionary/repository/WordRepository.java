package com.madr.external_dictionaries.monctionary.repository;

import com.madr.external_dictionaries.mongomodel.model.Word;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends MongoRepository<Word, String> {
}
