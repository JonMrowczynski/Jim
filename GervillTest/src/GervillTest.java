import javax.sound.midi.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class GervillTest {

    private static final int DRUM_CHANNEL = 9;
    private static final int TR_808_NUM = 56;

    public static void main(final String[] args) {
        try {

            final Synthesizer synthesizer = MidiSystem.getSynthesizer();
            if (!synthesizer.isOpen())
                synthesizer.open();
            synthesizer.unloadAllInstruments(synthesizer.getDefaultSoundbank());
            final InputStream soundfont = GervillTest.class.getResourceAsStream("FluidR3_GM.sf2");
            final Soundbank soundbank = MidiSystem.getSoundbank(new BufferedInputStream(soundfont));
            synthesizer.loadAllInstruments(soundbank);
            synthesizer.getChannels()[DRUM_CHANNEL].programChange(TR_808_NUM);

            final MidiDevice.Info synthesizerInfo = synthesizer.getDeviceInfo();
            System.out.println("Got Synthesizer ");
            System.out.println("\tName: " + synthesizerInfo.getName());
            System.out.println("\tDescription: " + synthesizerInfo.getDescription());
            System.out.println("\tVendor: " + synthesizerInfo.getVendor());
            System.out.println("\tVersion: " + synthesizerInfo.getVersion());

            final Receiver synthesizerReceiver = synthesizer.getReceiver();
            System.out.println("Got Synthesizer Receiver.");

            for (byte i = 0; i < Byte.MAX_VALUE; ++i) {
                final ShortMessage onMessage = new ShortMessage(ShortMessage.NOTE_ON, DRUM_CHANNEL, i, 100);
                synthesizerReceiver.send(onMessage, -1);
                System.out.println("Send message " + Arrays.toString(onMessage.getMessage()));
                Thread.sleep(250);
            }

        } catch (MidiUnavailableException | IOException | InvalidMidiDataException | InterruptedException e) { e.printStackTrace(); }
    }

} // end of class GervillTest
