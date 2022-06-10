package samples;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MediaTests {

	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String PATHFILE_AUDIO = "hal9000.wav";

	@Test void ansi_chars_test( ) {
		//
		// ⌐ ¬ ½ ¼ ¡ « »  ░ ▒ ▓ │ ┤ ╡ ╢ ╖ ╕ ╣ ║ ╗ ╝ ╜ ╛ ┐
		// └ ┴ ┬ ├ ─ ┼ ╞ ╟ ╚ ╔ ╩ ╦ ╠ ═ ╬ ╧ ╨ ╤ ╥ ╙ ╘ ╒ ╓ ╫ ╪ ┘ ┌ █ ▄ ▌ ▐ ▀
		// α ß Γ π Σ σ µ τ Φ Θ Ω δ ∞ φ ε ∩  ≡ ± ≥ ≤ ⌠ ⌡ ÷ ≈ ° ∙ · √ ⁿ ² ■
		System.out.println("ANSI chars_test");
		int[] colors = { 31, 208, 165, 33, 32, 36, 34, 35, 37 };
		String txtLine = "";
		for ( int color : colors ) {
			if ( color == 208 ) { txtLine += "\u001b[38;5;208m"; }
			if ( color == 165 ) { txtLine += "\u001b[38;5;172m"; }
			txtLine += "\u001b[" + color + ";1m" + "██";
		}
		System.out.println(txtLine);
		System.out.println("\u001b[38;2;255;165;0m" + "▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
		assertTrue(true);
	}

	@Test void audio_bell_test( ) {
		//
		System.out.println("ASCII bell");
		for ( int ictr = 0; ictr < 5; ictr++ ) {
			System.out.print('\u0007');
		}
		System.out.println();
	}

	@Test void audio_beep_test( ) {
		//
		System.out.println("Toolkit beep");
		for ( int ictr = 0; ictr < 5; ictr++ ) {
			//
			try {
				Toolkit.getDefaultToolkit().beep();
				Thread.sleep(1500);
			}
			catch (InterruptedException ex) {
				System.out.println("ERROR sleep: " + ex.getMessage());
			}
		}
	}

	@Test void audio_tone_test( ) {
		//
		System.out.println("AudioFormat tone");
		// https://stackoverflow.com/questions/34611134/java-beep-sound-produce-sound-of-some-specific-frequencies
		float SAMPLE_RATE = 8000f;
		int hertz = 15000;
		int msecs = 1000;
		double volume = .05;
		byte[] bytes = new byte[1];
		AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
		SourceDataLine sourceDataLine = null;
		try {
			sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
			sourceDataLine.open(audioFormat);
		}
		catch (LineUnavailableException ex) {
			System.out.println("ERROR tone: " + ex.getMessage());
		}
		sourceDataLine.start();
		double angle ;
		double val ;
		for ( double ictr = 0; ictr < msecs * 4; ictr = ictr + .02 ) {
			angle = ictr / ( SAMPLE_RATE / hertz ) * ( ictr / 80 ) * Math.PI;
			val = Math.sin(angle) * 127.0 * volume;
			bytes[0] = (byte) val;
			System.out.print("\t" + Math.round(val));
			sourceDataLine.write(bytes, 0, 1);
		}
		sourceDataLine.drain();
		sourceDataLine.stop();
		sourceDataLine.close();
	}

	@Test void audio_file_test( ) {
		//
		System.out.println("AudioInputStream file");
		String filePath = PATHFILE_LOCAL + PATHFILE_AUDIO;
		System.out.println("filePath: " + filePath);
		File file = new File(filePath).getAbsoluteFile();
		AudioInputStream AIS;
		try {
			AIS = AudioSystem.getAudioInputStream(file);
			Clip clip = AudioSystem.getClip();
			clip.open(AIS);
			float len = clip.getMicrosecondLength() / 1000f;
			clip.setMicrosecondPosition(0);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			System.out.println("clip len: " + len);
			//
			clip.start();
			Thread.sleep(( (int) len ) + 1000);
			clip.close();
		}
		catch (UnsupportedAudioFileException | IOException | LineUnavailableException |
		       InterruptedException ex) {
			System.out.println("ERROR file: " + ex.getMessage());
		}
	}
}
