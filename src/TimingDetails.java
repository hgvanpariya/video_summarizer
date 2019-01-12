
/*
 * Copyright 1999-2013 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import net.sf.classifier4J.summariser.ISummariser;
import net.sf.classifier4J.summariser.SimpleSummariser;

/**
 * A simple example that shows how to transcribe a continuous audio file that
 * has multiple utterances in it.
 */
public class TimingDetails {

    static ISummariser summariser = new SimpleSummariser();

    static Map<String, Float> timingDetails = new HashMap<>();
    static Map<String, Float> timingEndDetails = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Loading models...");

        Configuration configuration = new Configuration();

        // Load model from the jar
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");

        // You can also load model from folder
        // configuration.setAcousticModelPath("file:en-us");

        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
//        InputStream stream = TimingDetails.class
//                .getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
        String inputAudioFile = "/Users/hari/ML/videoSummarizer/spnixVoice/sphinx4/sphinx4-samples/src/main/resources/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav";
        FileInputStream stream = new FileInputStream(new File(
                inputAudioFile));
//        stream.skip(44);

        // Simple recognition with generic model
        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {

            System.out.format("Hypothesis: %s\n", result.getHypothesis());

            System.out.println("List of recognized words and their times:");
//            for (WordResult r : result.getWords()) {
//                System.out.println(r);
//            }

//            System.out.println("Total audio time " + result.getResult().getTotalAudioTime());
            System.out.println("collect time " + result.getResult().getCollectTime());
            System.out.println(">>" + result.getResult().getTimedBestResult(true));

            List<WordResult> timedBestResult = result.getResult().getTimedBestResult(false);
            float start = timedBestResult.get(0).getTimeFrame().getStart() / 1000;
            float end = (timedBestResult.get(timedBestResult.size() - 1).getTimeFrame().getEnd() / 1000) + 1;
            timingDetails.put(result.getHypothesis(), start);
            timingEndDetails.put(result.getHypothesis(), end);

            System.out.println("Best 3 hypothesis:");

        }
        recognizer.stopRecognition();

        System.out.println(timingDetails);

        StringBuilder completeString = new StringBuilder();
        for (String eachLine : timingDetails.keySet()) {
            completeString.append(eachLine).append(". ");
        }

        String summarise = summariser.summarise(completeString.toString(), 2);
        String[] split = summarise.split("\\.[ ]*");
        StringBuilder allFileList = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
 
            float startTime = timingDetails.get(string);
            float endTime = timingEndDetails.get(string);
            System.out.println("Summary : " + string);
            System.out.println("Start time : " + startTime + " End time : " + endTime);
//            System.out.println("End time : " + endTime);
            String commandToRun = "/Users/hari/ML/videoSummarizer/ffmpeg-20190112-ad0d5d7-macos64-static/bin/ffmpeg -i "+inputAudioFile+" -ss "+(int)startTime+" -t "+(int)(endTime-startTime)+" /Users/hari/ML/videoSummarizer/audioFiles/TextTo"+i+".wav";
            System.out.println(commandToRun);
//            Process p = Runtime.getRuntime().exec(commandToRun);
            Process p = Runtime.getRuntime().exec(new String[]{"csh","-c",commandToRun});

//            final int returnCode = p.waitFor();
            allFileList.append("file '/Users/hari/ML/videoSummarizer/audioFiles/TextTo"+i+".wav'\n");

        }
        
        Path createTempFile = Files.createTempFile( "tmp_", "");
        Path write = Files.write(createTempFile, allFileList.toString().getBytes(), StandardOpenOption.CREATE);
        File file = new File(write.toAbsolutePath().toString());
        
        System.out.println(file.toPath().toString()); 

        String commandToRunTransfer = "/Users/hari/ML/videoSummarizer/ffmpeg-20190112-ad0d5d7-macos64-static/bin/ffmpeg -f concat -safe 0 -i "+file.toPath().toString()+" -c copy "+"/Users/hari/ML/videoSummarizer/audioFiles/"+"output.wav";
        System.out.println(commandToRunTransfer);
        Process p = Runtime.getRuntime().exec(new String[]{"csh","-c",commandToRunTransfer});

//        Process p = Runtime.getRuntime().exec(commandToRunTransfer);


//        // Live adaptation to speaker with speaker profiles
//
//        stream = TimingDetails.class
//                .getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
//        stream.skip(44);
//
//        // Stats class is used to collect speaker-specific data
//        Stats stats = recognizer.createStats(1);
//        recognizer.startRecognition(stream);
//        while ((result = recognizer.getResult()) != null) {
//            stats.collect(result);
//        }
//        recognizer.stopRecognition();
//
//        // Transform represents the speech profile
//        Transform transform = stats.createTransform();
//        recognizer.setTransform(transform);
//
//        // Decode again with updated transform
//        stream = TimingDetails.class
//                .getResourceAsStream("/edu/cmu/sphinx/demo/aligner/10001-90210-01803.wav");
//        stream.skip(44);
//        recognizer.startRecognition(stream);
//        recognizer.getResult().getResult().setTotalAudioTime(0);
//        while ((result = recognizer.getResult()) != null) {
//            System.out.format("Hypothesis: %s\n", result.getHypothesis());
//            System.out.format("Hypothesis Time: %s\n", result.getResult().getTotalAudioTime());
//            
//        }
//        recognizer.stopRecognition();

    }
}
