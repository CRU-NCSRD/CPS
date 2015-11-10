/*
 * Copyright (C) 2015 Computational Systems & Human Mind Research Unit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.iit.demokritos.cru.cps.utilities.snowball;

import java.lang.reflect.Method;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class TestApp {

    private static void usage() {
        System.err.println("Usage: TestApp <algorithm> <input file> [-o <output file>]");
    }

    public static void main(String[] args) throws Throwable {

        Class stemClass = Class.forName("snowball.ext.englishStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
        stemmer.setCurrent("publisher");
        stemmer.stem();
        System.out.println(stemmer.getCurrent());
        stemmer.setCurrent("yawning");
        stemmer.stem();
        System.out.println(stemmer.getCurrent());
        /*
         Reader reader;
         reader = new InputStreamReader(new FileInputStream(args[1]));
         reader = new BufferedReader(reader);

         StringBuffer input = new StringBuffer();

         OutputStream outstream;

         if (args.length > 2) {
         if (args.length >= 4 && args[2].equals("-o")) {
         outstream = new FileOutputStream(args[3]);
         } else {
         usage();
         return;
         }
         } else {
         outstream = System.out;
         }
         Writer output = new OutputStreamWriter(outstream);
         output = new BufferedWriter(output);

         int repeat = 1;
         if (args.length > 4) {
         repeat = Integer.parseInt(args[4]);
         }

         Object [] emptyArgs = new Object[0];
         int character;
         while ((character = reader.read()) != -1) {
         char ch = (char) character;
         if (Character.isWhitespace((char) ch)) {
         if (input.length() > 0) {
         stemmer.setCurrent(input.toString());
         for (int i = repeat; i != 0; i--) {
         stemmer.stem();
         }
         output.write(stemmer.getCurrent());
         output.write('\n');
         input.delete(0, input.length());
         }
         } else {
         input.append(Character.toLowerCase(ch));
         }
         }
         output.flush();*/

    }
}
