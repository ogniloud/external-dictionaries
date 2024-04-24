package com.madr.external_dictionaries.wordmaster.kafka.serdes;

import com.google.protobuf.InvalidProtocolBufferException;
import com.madr.external_dictionaries.protobuf.Requests;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class WiktionaryRequestDeserializer implements Deserializer<Requests.WiktionaryRequest> {
    @Override
    public Requests.WiktionaryRequest deserialize(String s, byte[] bytes) {
        try {
            return Requests.WiktionaryRequest.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Error when deserializing byte[] to protobuf", e);
        }
    }
}
