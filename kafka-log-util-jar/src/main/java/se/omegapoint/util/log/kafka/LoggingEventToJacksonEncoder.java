package se.omegapoint.util.log.kafka;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ch.qos.logback.classic.spi.ILoggingEvent;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

/**
 * Created with IntelliJ IDEA.
 * User: geospa
 * Date: 29/04/14
 * Time: 18:51
 *
 * @TODO Webapp to view/process logs?!
 */
public class LoggingEventToJacksonEncoder implements Encoder<ILoggingEvent>{
    private final ObjectMapper mapper;

    public LoggingEventToJacksonEncoder(VerifiableProperties verifiableProperties){
        mapper=new ObjectMapper();

        SimpleModule testModule = new SimpleModule("DemoModule", VersionUtil.versionFor(this.getClass()));
        testModule.addSerializer(StackTraceElement.class,new JsonSerializer<StackTraceElement>() {
            @Override
            public void serialize(StackTraceElement value, JsonGenerator jgen, SerializerProvider provider){}
        });
        mapper.registerModule(testModule);

        setSerializationFeatures(verifiableProperties);
    }

    private void setSerializationFeatures(VerifiableProperties verifiableProperties) {
        for(SerializationFeature ft:SerializationFeature.values()){
            setSerializationFeature(verifiableProperties, ft);
        }
    }

    private void setSerializationFeature(VerifiableProperties verifiableProperties, SerializationFeature ft) {
        final String name = ft.getClass().getName()+"."+ft.name();
        if(verifiableProperties.containsKey(name)){
            final String value = verifiableProperties.getString(name);
            setSerializationFeature(ft, value);
        }
    }

    private void setSerializationFeature(SerializationFeature ft, String value) {
        if(!ft.enabledByDefault() && Boolean.valueOf(value)){
            mapper.enable(ft);
        }
        if(ft.enabledByDefault() && !Boolean.valueOf(value)){
            mapper.disable(ft);
        }
    }

    @Override
    public byte[] toBytes(ILoggingEvent iLoggingEvent) {
        try {
            return mapper.writeValueAsBytes(iLoggingEvent.getLoggerContextVO());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize iLoggingEvent:"+iLoggingEvent+" to string.", e);
        }
    }
}
