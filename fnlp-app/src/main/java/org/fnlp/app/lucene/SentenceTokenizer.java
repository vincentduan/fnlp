/**
*  This file is part of FNLP (formerly FudanNLP).
*  
*  FNLP is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*  
*  FNLP is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*  
*  You should have received a copy of the GNU General Public License
*  along with FudanNLP.  If not, see <http://www.gnu.org/licenses/>.
*  
*  Copyright 2009-2014 www.fnlp.org. All rights reserved. 
*/

package org.fnlp.app.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

public final class SentenceTokenizer extends Tokenizer {

  /**
   * End of sentence punctuation: 。，！？；,!?;
   */
  private final static String PUNCTION = "。，！？；,!?;";
  
  /**
   * Space-like characters that need to be skipped: such as space, tab, newline, carriage return.
   */
  public static final String SPACES = " \t\r\n";

  private final StringBuilder buffer = new StringBuilder();

  private int tokenStart = 0, tokenEnd = 0;
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

  public SentenceTokenizer(Reader reader) {
    super(reader);
  }


  public SentenceTokenizer(AttributeFactory factory, Reader reader) {
    super(factory, reader);
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    clearAttributes();
    buffer.setLength(0);
    int ci;
    char ch, pch;
    boolean atBegin = true;
    tokenStart = tokenEnd;
    ci = input.read();
    ch = (char) ci;

    while (true) {
      if (ci == -1) {
        break;
      } else if (PUNCTION.indexOf(ch) != -1) {
        // End of a sentence
        buffer.append(ch);
        tokenEnd++;
        break;
      } else if (atBegin && SPACES.indexOf(ch) != -1) {
        tokenStart++;
        tokenEnd++;
        ci = input.read();
        ch = (char) ci;
      } else {
        buffer.append(ch);
        atBegin = false;
        tokenEnd++;
        pch = ch;
        ci = input.read();
        ch = (char) ci;
        // Two spaces, such as CR, LF
        if (SPACES.indexOf(ch) != -1
            && SPACES.indexOf(pch) != -1) {
          // buffer.append(ch);
          tokenEnd++;
          break;
        }
      }
    }
    if (buffer.length() == 0)
      return false;
    else {
      termAtt.setEmpty().append(buffer);
      offsetAtt.setOffset(correctOffset(tokenStart), correctOffset(tokenEnd));
      typeAtt.setType("sentence");
      return true;
    }
  }

  @Override
  public void reset() throws IOException {
	  super.reset();
    tokenStart = tokenEnd = 0;
  }

  @Override
  public void end() {
    // set final offset
    final int finalOffset = correctOffset(tokenEnd);
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
}