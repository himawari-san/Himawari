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

import java.util.Comparator;
import java.nio.*;


public class ArgumentStrComparator implements Comparator<Object> {

  private MappedByteBuffer buf;
  private int mbLimit;
  private byte b2[] = new byte[2];

  public ArgumentStrComparator(MappedByteBuffer buf) {
    this.buf = buf;
    mbLimit = buf.limit();
  }
  public int compare(Object o1, Object o2) {
    int p1 = ((Integer)o1).intValue()*2;
    int p2 = ((Integer)o2).intValue()*2;
    int c1, c2;

    // little endian の UNICODE を想定
    while(true){
      // タグを読み飛ばす(c1)
      buf.position(p1);
      c1 = getDecodedChar();
      p1 = buf.position();

      // タグを読み飛ばす(c2)
      buf.position(p2);
      c2 = getDecodedChar();
      p2 = buf.position();

//      if(c1 < 0 || c2 < 0){
//        System.out.println("aaa");
//      }

      if(c1 == '"'){
//        return -1;
//        return -(int)(Math.random()*100);
        if(c2 == '"'){
          if(Math.random()< 0.5){
            return 1;
          } else {
            return -1;
          }
        }
        return c1-c2;
      } else if(c2 == '"'){
//        return 1;
//        return (int)(Math.random()*100);
        return c1-c2;
      } else if(c1 == c2){
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