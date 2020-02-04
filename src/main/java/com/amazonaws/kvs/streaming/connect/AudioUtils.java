package com.amazonaws.kvs.streaming.connect;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Utility class to download/upload audio files from/to S3
 *
 * <p>
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * </p>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public final class AudioUtils {

	// 8KHz, 16 bit, 1 channel, signed, little-endian
	public static final AudioFormat AMAZON_CONNECT_AUDIO_FORMAT = new AudioFormat(8000, 16, 1, true, false);

	/**
	 * Converts the given raw audio data into a wav file. Returns the wav file back.
	 */
	public static File convertRawToWav(String audioFilePath) throws IOException, UnsupportedAudioFileException {
		File outputFile = new File(audioFilePath.replace(".raw", ".wav"));
		AudioInputStream source = new AudioInputStream(Files.newInputStream(Paths.get(audioFilePath)),
				AMAZON_CONNECT_AUDIO_FORMAT, -1);
		AudioSystem.write(source, AudioFileFormat.Type.WAVE, outputFile);
		return outputFile;
	}

	/**
	 * Converts the given raw audio data into a wav file. Returns the wav file back.
	 */
	public static byte[] convertRawToWavFileBytes(String audioFilePath)
			throws IOException, UnsupportedAudioFileException {
		File file = convertRawToWav(audioFilePath);
		return Files.readAllBytes(file.toPath());
	}

	/**
	 * Converts the given raw audio data into a wav file. Returns the wav file back.
	 */
	public static void convertRawToWavFile(byte[] rawAudiobytes, File destFile)
			throws IOException, UnsupportedAudioFileException {
		AudioInputStream source = new AudioInputStream(new ByteArrayInputStream(rawAudiobytes),
				AMAZON_CONNECT_AUDIO_FORMAT, -1);
		AudioSystem.write(source, AudioFileFormat.Type.WAVE, destFile);
	}

}