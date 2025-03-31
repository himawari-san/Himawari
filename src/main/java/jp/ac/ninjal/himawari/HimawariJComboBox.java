/*
    Copyright (C) 2004-2025 Masaya YAMAGUCHI, Hajime Osada

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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * <p>タイトル: HimawariJComboBox</p>
 * <p>説明:ひまわり用に拡張したJComboBoxクラス</p>
 * <p>著作権: Copyright (c) 2011</p>
 * <p>会社名: トライアックス（株）</p>
 *
 * @author Hajime Osada
 * @version 1.0
 */
public class HimawariJComboBox extends JComboBox<String> {

	private static final long serialVersionUID = -3649206379616584501L;

	//arrowButtonのマージン
	private static final int MARGIN = 30;

	//フォントサイズのオフセット：フォントサイズ調整時の最小単位
	private static final float FONT_SIZE_OFFSET = (float)0.5;

	//comboboxの幅
	private int boxWidth;

	//矢印の幅
	private int arrowWidth;


	/**
	 * フォントの自動調整
	 */
	public void adjustFontAuto(){

		//デフォルトの幅だと文字が切れるため、デフォルトに+arrowbottun分プラス
		int prefWidth = getPreferredSize().width + getArrowWidth();

		//コンボボックスの幅の取得
		boxWidth = prefWidth > getMaximumSize().width ? getMaximumSize().width : prefWidth;
//		System.err.println("prefWidth:" + prefWidth);
//		System.err.println("max:" + getMaximumSize().width);
//		boxWidth = getMaximumSize().width;
		
		//コンボボックスの幅の設定
		setSize(new Dimension(boxWidth, getPreferredSize().height));
//		setPreferredSize(new Dimension(boxWidth, getPreferredSize().height));

		//矢印ボタンの幅
		arrowWidth = getArrowWidth() + MARGIN;

		//最大長のjLabelオブジェクトを取得する
		JLabel maxLengthLabel = getMaxLengthLabel();

		//最大幅の取得
		int maxWidth =  maxLengthLabel.getPreferredSize().width;

		//最大長オブジェクト幅がテキスト表示エリアの幅を
		//越えている場合、フォントの調整を行う。
		if(maxWidth > boxWidth - arrowWidth){

			//最適なフォントサイズの計算
			float fontSize = calcBestFontSize(maxLengthLabel);

			//フォントのセット
			setFont(getFont().deriveFont(fontSize));
		}
	}


	/**
	 * arrowButtonの幅を取得する
	 * @return arrowButtonの幅
	 */
	private int getArrowWidth() {

		int arrowWidth = 0;
		int componentCount = getComponentCount();

		//combobox内の全てのコンポーネントを取得
		for(int i = 0; i < componentCount; i ++){
			Component component = getComponent(i);

			//コンポーネントが矢印ボタンのインスタンスの場合
			if(component instanceof BasicArrowButton){
				BasicArrowButton arrowButton = (BasicArrowButton) component;
				arrowWidth = arrowButton.getPreferredSize().width;
				break;
			}
		}
		return arrowWidth;
	}


	/**
	 * 全てのitemの中で最大長のJLabelオブジェクトを取得する
	 * @return 最大長のJLabelオブジェクト
	 */
	private JLabel getMaxLengthLabel() {
		int maxWidth = 0;
		JLabel ret = new JLabel();

		/**
		 * 全ての要素分ループし、最大の長さのものを取得
		 */
		for(int i = 0; i < getItemCount(); i ++){

			//幅の取得をするため、JLabelオブジェクトを生成
			JLabel label = new JLabel((String) getItemAt(i));
			label.setFont(getFont());

			//JLabelオブジェクトから幅を取得
			int width = label.getPreferredSize().width;

			//長さが最大のものを取得
			if(width > maxWidth){
				maxWidth = width;
				ret = label;
			}
		}
		return ret;
	}


	/**
	 * 最適なフォントサイズを計算する。
	 * @param target 対象JLabelオブジェクト
	 * @return 最適フォントサイズ
	 */
	private float calcBestFontSize(JLabel target) {

		//デフォルトフォント
		Font defaultFont = getFont();
		//フォントサイズ
		float fontSize = defaultFont.getSize2D();

		//返却値
		float ret = 0;

		try{
			//現在の文字幅の取得
			int width = getPreferredSize().width;

			/**
			 * 対象文字列の幅が、最大幅からarrowButton分の幅を
			 * 引いた幅より小さくなるまで繰り返し
			 */
			while(width > boxWidth - arrowWidth){

				//フォントサイズを小さくする（オフセット分）
				fontSize -=  FONT_SIZE_OFFSET;

				//小さくしたフォントをセット
				target.setFont(defaultFont.deriveFont(fontSize));

				//幅の取得
				width = target.getPreferredSize().width;
			}

			//返却値のセット
			ret = fontSize;

		}catch(Exception e){
			//エラーが発生した場合はデフォルトフォントを返却する
			ret = defaultFont.getSize2D();
		}
		return ret;
	}



}