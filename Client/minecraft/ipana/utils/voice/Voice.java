package ipana.utils.voice;

//import edu.cmu.sphinx.api.LiveSpeechRecognizer;
//import edu.cmu.sphinx.api.SpeechResult;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import net.minecraft.client.Minecraft;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Voice {
    //public LiveSpeechRecognizer recognizer;
    public String lastSaid = "";
    public String moduleInfo = "";
    private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(1);

    public Voice() {
        /*
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");

        configuration.setGrammarPath("resource:/ipana/utils/voice");
        configuration.setGrammarName("grammar");
        configuration.setUseGrammar(true);

        try {
            if (AudioSystem.isLineSupported(Port.Info.MICROPHONE)) {
                recognizer = new LiveSpeechRecognizer(configuration);
                recognizer.startRecognition(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
    }

    public void tick() {
        /*
        eventsExecutorService.submit(() -> {
            if (AudioSystem.isLineSupported(Port.Info.MICROPHONE) && recognizer != null) {
                SpeechResult speechResult = recognizer.getResult();
                if (speechResult != null) {
                    lastSaid = speechResult.getHypothesis();
                    if (Minecraft.getMinecraft().gameSettings.keyBindVoiceAction.pressed) {
                        for (Module module : ModuleManager.getModuleList()) {
                            String name = lastSaid.replace(" ", "");
                            if (name.equals("aura")) {
                                name = "killaura";
                            } else if (name.equals("antiknockback")) {
                                name = "antikb";
                            }
                            if (module.getName().toLowerCase().contains(name)) {
                                module.toggle();
                                moduleInfo = module.getName()+" "+(module.isEnabled() ? "§aEnabled" : "§cDisabled");
                            }
                        }
                    } else {
                        lastSaid = "";
                    }
                    System.out.println(lastSaid);
                }
            }
        });

         */
    }
}
