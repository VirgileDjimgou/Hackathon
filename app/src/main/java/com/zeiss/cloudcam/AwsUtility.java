package com.zeiss.cloudcam;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Created by zoafro on 24.01.2017.
 */
public class AwsUtility {

    // private final static String IDENTITY_POOL_ID = "eu-central-1:63a5bfb7-461d-428f-bf76-73e1b7a18ef8";
    // private final static String  IDENTITY_POOL_ID  = "AKIAII3RI56U56HW2UAA:RMj51prMo4ysS7H65rAdHCm0QerC2+x2PzEiYXxw";
    private final static String  IDENTITY_POOL_ID = "eu-west-1_ygH6puu6A";
    // private final static String IDENTITY_POOL_ID = " 2001:db8:1234:1a00:9691:9503:25ad:1761/128";

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */

    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    IDENTITY_POOL_ID,
                    //Regions.EU_CENTRAL_1);
                    Regions.AP_SOUTHEAST_1);
        }

        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    // Definition for amazon Client
    // schlusssel  for Acccount   SSh Schlussel  muss hier eingegeben werden
    public String  AmazonSSh(){
         // int  StringToReturn = "leer Zeischen ";
         return  "echo";
    }



    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        }

        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }
}
