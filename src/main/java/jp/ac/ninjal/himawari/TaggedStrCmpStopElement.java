/*
    Copyright (C) 2004-2025 Masaya YAMAGUCHI

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jp.ac.ninjal.himawari;

import java.util.*;
import java.nio.MappedByteBuffer;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2003-2005</p>
 *
 * <p>会社名: </p>
 *
 * @author Masaya YAMAGUCHI
 * @version 0.9.4
 */
public class TaggedStrCmpStopElement implements Comparator<Object> {

  MappedByteBuffer buf;
  private int mbLimit;
  private byte b2[] = new byte[2];
  private String stopElementName;

  public TaggedStrCmpStopElement(MappedByteBuffer buf, String stopElementName) {
    this.buf = buf;
    this.stopElementName = stopElementName;
    mbLimit = buf.limit();
//    System.out.println("ss0"); //$NON-NLS-1$
  }

  public int compare(Object o1, Object o2) {
    int p1 = ( (Integer) o1).intValue() * 2;
    int p2 = ( (Integer) o2).intValue() * 2;
    int c1, c2;
    boolean tagFlag;
    StringBuilder sb = new StringBuilder();

    // little endian の UNICODE を想定
    while (true) {
      // タグを読み飛ばす(c1)
      tagFlag = false;
      buf.position(p1);
      while (true) {
        c1 = getDecodedChar();
        if (c1 == '<') {
          tagFlag = true;
        } else if (c1 == '>') {
          String tagname = sb.toString();
          if(tagname.compareTo(stopElementName) == 0 ||
             tagname.startsWith(stopElementName + " ")){ //$NON-NLS-1$
            c1 = 0;
//            System.out.println("ss"); //$NON-NLS-1$
          }
          sb.delete(0, sb.length());
          tagFlag = false;
        } else if (!tagFlag) {
          break;
        } else {
          sb.append(c1);
        }
      }
      p1 = buf.position();

      // タグを読み飛ばす(c2)
      tagFlag = false;
      buf.position(p2);
      while (true) {
        c2 = getDecodedChar();
        if (c2 == '<') {
          tagFlag = true;
        } else if (c2 == '>') {
          String tagname = sb.toString();
          if(tagname.compareTo("/" + stopElementName) == 0 || //$NON-NLS-1$
             tagname.startsWith("/" + stopElementName + " ")){ //$NON-NLS-1$ //$NON-NLS-2$
            c2 = 0;
          }
          sb.delete(0, sb.length());
          tagFlag = false;
        } else if (!tagFlag) {
          break;
        }
      }
      p2 = buf.position();

      if (c1 == c2) {
        if (p1 >= mbLimit - 2) {
          return -c2;
        } else if (p2 >= mbLimit - 2) {
          return c1;
        }
      } else {
        return c1 - c2;
      }
    }
  }

  public boolean equals(Object obj) {
    return this.equals(obj);
  }

  // UTF-16(Little Endian) 専用
  private int getDecodedChar() {
    try {
      buf.get(b2);
    }
    catch (Exception e) {
      return -1;
    }
    return (int) ( ( (0xff & b2[1]) << 8) | (0xff & b2[0]));
  }

}
