package com.madr.external_dictionaries.wordmaster.kafka.serdes;

import com.madr.external_dictionaries.mongomodel.protobuf.Responses;
import org.apache.kafka.common.serialization.Serializer;

public class WiktionaryResponseSerializer implements Serializer<Responses.WiktionaryResponse> {
    @Override
    public byte[] serialize(String s, Responses.WiktionaryResponse response) {
        return response.toByteArray();
    }
}
