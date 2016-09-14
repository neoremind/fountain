package net.neoremind.fountain.util;

import java.io.Closeable;
import java.io.IOException;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.io.SegmentedStringWriter;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.smile.SmileFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.util.ByteArrayBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * JSON工具类
 * 
 * @author Zhang Xu
 * @version 2013-4-17 下午4:25:22
 */
public abstract class JsonUtils {

	private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

	
	private static class ObjectMapperExt extends ObjectMapper {
	    public ObjectMapperExt(){
	        super();
	    }
	    public ObjectMapperExt(JsonFactory jf){
	        super(jf);
	    }
		public String writeValueAsString(Object value,
				JsonSerialize.Inclusion inc) throws IOException,
				JsonGenerationException, JsonMappingException {
			if (inc == null) {
				return super.writeValueAsString(value);
			}
			// alas, we have to pull the recycler directly here...
			SegmentedStringWriter sw = new SegmentedStringWriter(
					_jsonFactory._getBufferRecycler());
			writeValueWithConf(_jsonFactory.createJsonGenerator(sw), value,
					inc);
			return sw.getAndClear();
		}

		public byte[] writeValueAsBytes(Object value,
                JsonSerialize.Inclusion inc) throws IOException,
                JsonGenerationException, JsonMappingException {
            if (inc == null) {
                return super.writeValueAsBytes(value);
            }
            // alas, we have to pull the recycler directly here...
            ByteArrayBuilder bb = new ByteArrayBuilder(_jsonFactory._getBufferRecycler());
            writeValueWithConf(_jsonFactory.createJsonGenerator(bb, JsonEncoding.UTF8), value,inc);
            byte[] result = bb.toByteArray();
            bb.release();
            return result;
        }
		private void configAndWriteCloseable(JsonGenerator jgen, Object value,
				SerializationConfig cfg) throws IOException,
				JsonGenerationException, JsonMappingException {
			Closeable toClose = (Closeable) value;
			try {
				_serializerProvider.serializeValue(cfg, jgen, value,
						_serializerFactory);
				JsonGenerator tmpJgen = jgen;
				jgen = null;
				tmpJgen.close();
				Closeable tmpToClose = toClose;
				toClose = null;
				tmpToClose.close();
			} finally {
				/*
				 * Need to close both generator and value, as long as they
				 * haven't yet been closed
				 */
				if (jgen != null) {
					try {
						jgen.close();
					} catch (IOException ioe) {
					}
				}
				if (toClose != null) {
					try {
						toClose.close();
					} catch (IOException ioe) {
					}
				}
			}
		}

		private void writeValueWithConf(JsonGenerator jgen, Object value,
				JsonSerialize.Inclusion inc) throws IOException,
				JsonGenerationException, JsonMappingException {
			
			SerializationConfig cfg = copySerializationConfig();
			cfg = cfg.withSerializationInclusion(inc);
			
			// [JACKSON-96]: allow enabling pretty printing for ObjectMapper
			// directly
			if (cfg.isEnabled(SerializationConfig.Feature.INDENT_OUTPUT)) {
				jgen.useDefaultPrettyPrinter();
			}
			// [JACKSON-282]: consider Closeable
			if (cfg.isEnabled(SerializationConfig.Feature.CLOSE_CLOSEABLE)
					&& (value instanceof Closeable)) {
				configAndWriteCloseable(jgen, value, cfg);
				return;
			}
			boolean closed = false;
			try {
				_serializerProvider.serializeValue(cfg, jgen, value,
						_serializerFactory);
				closed = true;
				jgen.close();
			} finally {
				/*
				 * won't try to close twice; also, must catch exception (so it
				 * will not mask exception that is pending)
				 */
				if (!closed) {
					try {
						jgen.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
	}

	private final static ObjectMapperExt objectMapper = new ObjectMapperExt();
	private final static ObjectMapperExt objectMapperByte = new ObjectMapperExt(new SmileFactory());

	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	/**
	 * JSON串转换为Java泛型对象，可以是各种类型，此方法最为强大。用法看测试用例。
	 * 
	 * @param <T>
	 * @param jsonString
	 *            JSON字符串
	 * @param tr
	 *            TypeReference,例如: new TypeReference< List<FamousUser> >(){}
	 * @return List对象列表
	 */
	@SuppressWarnings("unchecked")
	public static <T> T json2GenericObject(String jsonString,
			TypeReference<T> tr) {

		if (jsonString == null || "".equals(jsonString)) {
			return null;
		} else {
			try {
				return (T) objectMapper.readValue(jsonString, tr);
			} catch (Exception e) {
				log.warn("json error:" + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * Java对象转Json字符串
	 */
	public static String toJson(Object object) {
		String jsonString = "";
		try {
			jsonString = objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			log.warn("json error:" + e.getMessage());
		}
		return jsonString;

	}
	public static byte[] toJsonBytes(Object object) {
	    byte[] smileData = null;
        try {
            smileData = objectMapperByte.writeValueAsBytes(object);
        } catch (Exception e) {
            log.warn("json error:" + e.getMessage());
        }
        return smileData;

    }

	public static String toJson(Object object, boolean ignoreEmpty) {
		String jsonString = "";
		try {
			if(ignoreEmpty){
				jsonString = objectMapper.writeValueAsString(object,JsonSerialize.Inclusion.NON_EMPTY);
			}else{
				jsonString = objectMapper.writeValueAsString(object);
			}
			} catch (Exception e) {
			log.warn("json error:" + e.getMessage());
		}
		return jsonString;
		
	}
	public static byte[] toJsonBytes(Object object, boolean ignoreEmpty) {
        byte[] smileData = null;
        try {
            if(ignoreEmpty){
                smileData = objectMapperByte.writeValueAsBytes(object,JsonSerialize.Inclusion.NON_EMPTY);
            }else{
                smileData = objectMapperByte.writeValueAsBytes(object);
            }
        } catch (Exception e) {
            log.warn("json error:" + e.getMessage());
        }
        return smileData;

    }
	/**
	 * Json字符串转Java对象
	 */
	@SuppressWarnings("deprecation")
    public static Object json2Object(String jsonString, Class<?> c) {

		if (jsonString == null || "".equals(jsonString)) {
			return "";
		} else {
			try {
				objectMapper.getDeserializationConfig().set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				return objectMapper.readValue(jsonString, c);
			} catch (Exception e) {
				log.error("json error:" + e.getMessage());
				throw new RuntimeException(e);
			}

		}
	}
	@SuppressWarnings("deprecation")
    public static Object bytes2Object(byte[] jsonBytes, Class<?> c) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        } else {
            try {
                objectMapperByte.getDeserializationConfig().set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapperByte.readValue(jsonBytes, c);
            } catch (Exception e) {
                log.error("json error:" + e.getMessage());
                throw new RuntimeException(e);
            }

        }
    }
	@SuppressWarnings("deprecation")
    public static Object json2Object(String jsonString, JavaType type) {

		if (jsonString == null || "".equals(jsonString)) {
			return "";
		} else {
			try {
				objectMapper.getDeserializationConfig().set(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				return objectMapper.readValue(jsonString, type);
			} catch (Exception e) {
				log.warn("json error:" + e.getMessage());
			}

		}
		return "";
	}
	public static Object convertValue(Object val,JavaType type){
		return objectMapper.convertValue(val, type);
	}
}
