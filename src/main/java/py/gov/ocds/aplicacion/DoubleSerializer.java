package py.gov.ocds.aplicacion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;

public class DoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator jgen, SerializerProvider provider) throws IOException{
        try {
            String aux = new BigDecimal(value).toPlainString();
            jgen.writeNumber(aux);
        } catch (ArithmeticException e) {
            jgen.writeNumber(value.longValue());
        }
    }
}