package com.hfad.ergasia3;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TtS {

    private TextToSpeech tts;
    //Create the Listener for text to speech conversion.
    private TextToSpeech.OnInitListener initListener =
            new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status==TextToSpeech.SUCCESS)
                        tts.setLanguage(Locale.ENGLISH);
                }
            };
    //Constructor
    public TtS(Context context){
        tts = new TextToSpeech(context,initListener);
    }
    //Method for converting text to speech.
    public void speak(String message){
        tts.setPitch(3);
        tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,null); //QUEUE_ADD
    }

}
