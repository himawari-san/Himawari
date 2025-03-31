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

 /**
 * @(#) TaggedStrCmp.java 1.00 2003-06
 *
 * Copyright 2003 NIJLA
 */

package jp.ac.ninjal.himawari;

import java.util.Comparator;
import java.nio.*;


/**
 *
 *
 * @author Masaya YAMAGUCHI
 * @version 1.0
 */

public class TaggedStrCmp implements Comparator<Object> {

  MappedByteBuffer buf;
  private int mbLimit;
  private byte b2[] = new byte[2];

  public TaggedStrCmp(MappedByteBuffer buf) {
    this.buf = buf;
    mbLimit = buf.limit();
  }

  public int compare(Object o1, Object o2) {
    int p1 = ((Integer)o1).intValue()*2;
    int p2 = ((Integer)o2).intValue()*2;
    int c1, c2;
    boolean tagFlag;


    // little endian の UNICODE を想定
    while(true){
      // タグを読み飛ばす(c1)
      tagFlag = false;
      buf.position(p1);
      while(true){
        c1 = getDecodedChar();
        if(c1 == '<'){
          tagFlag = true;
        } else if(c1 == '>'){
          tagFlag = false;
        } else if(!tagFlag){
          break;
        }
      }
      p1 = buf.position();

      // タグを読み飛ばす(c2)
      tagFlag = false;
      buf.position(p2);
      while(true){
        c2 = getDecodedChar();
        if(c2 == '<'){
          tagFlag = true;
        } else if(c2 == '>'){
          tagFlag = false;
        } else if(!tagFlag){
          break;
        }
      }
      p2 = buf.position();

      if(c1 == c2){
        if(p1 >= mbLimit-2){
//          System.err.println("Warining(SuffixStrCmp, compare): c1 = c2" + ", " + ((Integer)o1).intValue()+ ", " + ((Integer)o2).intValue());
          return -c2;
        } else if(p2 >= mbLimit-2){
//          System.err.println("Warining(SuffixStrCmp, compare): c1 = c2" + ", " + ((Integer)o1).intValue()+ ", " + ((Integer)o2).intValue());
          return c1;
        }
      } else {
        return c1-c2;
      }
    }
  }

  public boolean equals(Object obj) {
    return this.equals(obj);
  }


  // UTF-16(Little Endian) 専用
  private int getDecodedChar(){
    try{
      buf.get(b2);
    } catch (Exception e){
      return -1;
    }
    return (int)(((0xff & b2[1]) << 8) | (0xff & b2[0]));
  }
}
