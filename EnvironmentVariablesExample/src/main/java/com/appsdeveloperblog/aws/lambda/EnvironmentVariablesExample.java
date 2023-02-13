package com.appsdeveloperblog.aws.lambda;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.util.Base64;

/**
 * Handler for requests to Lambda function.
 */
public class EnvironmentVariablesExample implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final String MY_VARIABLE = decryptKey("MY_VARIABLE");
    private final String MY_COGNITO_USER_POOL_ID = decryptKey("MY_COGNITO_USER_POOL_ID");
    private final String MY_COGNITO_CLIENT_APP_SECRET = decryptKey("MY_COGNITO_CLIENT_APP_SECRET");
    private final String MY_GLOBAL_VARIABLE = decryptKey("MY_GLOBAL_VARIABLE");


    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        // NEVER LOG KEYS, THIS IS FOR TRAINING PURPOSES ONLY
        LambdaLogger logger = context.getLogger();
        logger.log("MY_VARIABLE = " + MY_VARIABLE);
        logger.log("MY_COGNITO_USER_POOL_ID = " + MY_COGNITO_USER_POOL_ID);
        logger.log("MY_COGNITO_CLIENT_APP_SECRET = " + MY_COGNITO_CLIENT_APP_SECRET);
        logger.log("MY_GLOBAL_VARIABLE = " + MY_GLOBAL_VARIABLE);

        return response
                .withBody("{}")
                .withStatusCode(500);
    }

    private String decryptKey(String name) {
        System.out.println("Decrypting key");
        byte[] encryptedKey = Base64.decode(System.getenv(name));
        System.out.println("Decoded key");
        Map<String, String> encryptionContext = new HashMap<>();
        encryptionContext.put("LambdaFunctionName",
                System.getenv("AWS_LAMBDA_FUNCTION_NAME"));

        AWSKMS client = AWSKMSClientBuilder.defaultClient();

        DecryptRequest request = new DecryptRequest()
                .withCiphertextBlob(ByteBuffer.wrap(encryptedKey))
                .withEncryptionContext(encryptionContext);

        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), StandardCharsets.UTF_8);
    }

}
