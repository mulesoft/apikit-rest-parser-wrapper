package org.mule.amf.impl.parser.rule;

import amf.client.validate.ValidationResult;
import amf.core.annotations.LexicalInformation;
import amf.core.parser.Position;
import amf.core.parser.Range;
import amf.core.validation.AMFValidationResult;
import org.junit.Test;
import scala.Option;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;



public class ApiValidationResultImplTest {

    public ValidationResult createAMFValidationResult(String message, Position startPosition, Option<String> location){
        Option<LexicalInformation> position = Option.apply(new LexicalInformation(new Range(startPosition, new Position(0,0))));
        AMFValidationResult amfValidationResult = new AMFValidationResult(message, "level", "targetNode", Option.empty(), "validationId", position, location, "");
        return new ValidationResult(amfValidationResult);
    }

    @Test
    public void testWithDetails() throws Exception{
        Position start = new Position(1, 1);
        Option<String> location = Option.apply("Here is the error");
        String message = "This is an error message!";
        ValidationResult validationResult = createAMFValidationResult(message, start, location);
        ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);

        String actualMessage = apiValidationResult.getMessage();
        String expectedMessage = "This is an error message!\n" +
                "  Location: Here is the error\n" +
                "  Position: Line 1,  Column 1";
        assertThat(actualMessage, is(expectedMessage));
    }

    @Test
    public void testWithoutDetails() throws Exception{
        Position start = new Position(0, 0);
        Option<String> location = Option.empty();
        String message = "This is an error message!";
        ValidationResult validationResult = createAMFValidationResult(message, start, location);
        ApiValidationResultImpl apiValidationResult = new ApiValidationResultImpl(validationResult);
        String actualMessage = apiValidationResult.getMessage();
        String expectedMessage = "This is an error message!";
        assertThat(actualMessage, is(expectedMessage));
    }
}
