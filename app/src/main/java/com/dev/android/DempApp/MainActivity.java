package com.dev.android.DempApp;

import androidx.appcompat.app.AppCompatActivity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ACCOUNT_PICKER = 01;
    private GoogleAccountCredential credential;
    private com.google.api.services.gmail.Gmail mService = null;
    private EditText toEmail,fromEmail,messageEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindActivity();
        settingUpCred();
    }

    private void bindActivity() {
        toEmail=findViewById(R.id.toEmail);
        fromEmail=findViewById(R.id.fromEmail);
        messageEmail=findViewById(R.id.messageEmail);
    }

    private MimeMessage createEmail(String to,String from,String message) throws MessagingException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);
        email.setFrom(fAddress);
        email.setText(message);
        email.addRecipient(javax.mail.Message.RecipientType.TO, tAddress);
        return email;
    }

    private void settingUpCred() {
        final String[] SCOPES = {
                GmailScopes.GMAIL_LABELS,
                GmailScopes.GMAIL_SEND

        };
   credential=GoogleAccountCredential.usingOAuth2(getApplicationContext()
   , Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff()).setSelectedAccountName("hvhardik0@gmail.com");
    }

    public void sendEmail(View view) {

        if(credential!=null){
            try {
                if(!toEmail.getText().toString().isEmpty() && !fromEmail.getText().toString().isEmpty() && !messageEmail.getText().toString().isEmpty()) {
                    startActivityForResult(credential.newChooseAccountIntent(),REQUEST_ACCOUNT_PICKER);
                    MimeMessage m = createEmail(toEmail.getText().toString(), fromEmail.getText().toString(), messageEmail.getText().toString());
                 AsyncTask<MimeMessage, Void, String> s= new MyAsync().execute(m);
                    AsyncTask.Status status=s.getStatus();
                    Log.e("Status",status.toString());
                }
                else{

                    Toast.makeText(this, "Please fill All the boxes", Toast.LENGTH_SHORT).show();
                }

            } catch (MessagingException e) {
                Log.e("MessageException","MSG"+e.getMessage());
            }
        }
    }


    class MyAsync extends AsyncTask<MimeMessage,Void,String> {




        public void  MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
        }


         @Override
         protected void onPreExecute() {
            Log.e("Logging","Entered");
            MakeRequestTask(credential);
         }

         private Message createMessageWithEmail(MimeMessage email) throws IOException, MessagingException {
             Log.e("Logging","EnteredCreateMessage");
             ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            email.writeTo(bytes);
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
            Message message = new Message();
            message.setRaw(encodedEmail);
            return message;
        }

        private String sendMessage(Gmail service, String userId, MimeMessage email)
                throws MessagingException, IOException {
            Log.e("Logging","sendMessage");
            Message message = createMessageWithEmail(email);
            message = service.users().messages().send(userId, message).execute();

            return message.getId();
        }


        @Override
        protected String doInBackground(MimeMessage... mimeMessages) {
            MimeMessage email=mimeMessages[0];
            String s=null;
            try {
                 s=sendMessage(mService,"hvhardik0@gmail.com",email);
            } catch (MessagingException e) {
                Log.e("MessageException","MSG"+e.getMessage());
            } catch (IOException e) {
                Log.e("MessageIOException","MSG"+e.getMessage());
            }
            return s;
        }
    }
}
