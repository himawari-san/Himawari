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

/**
 * タイトル:  太陽検索システム
 * 説明:
 * 著作権:   Copyright (c) 2001
 * 会社名:   独立行政法人国立国語研究所
 * @author Masaya YAMAGUCHI
 * @version 0.1
 */

public class Range {
  private int start;
  private int end;

  public Range(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public int getStart(){
    return start;
  }

  public int getEnd(){
    return end;
  }

  public int getValue(){
    return end - start;
  }
}