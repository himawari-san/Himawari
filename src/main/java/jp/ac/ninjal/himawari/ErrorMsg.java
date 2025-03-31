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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ErrorMsg {

	/************************************
	 * 定数
	 ************************************/

	//=====エラー原因コード======//

	//コーパスオープンエラー
	public final static int ERR_COURPUS_OPEN = 111;
	//コーパスクローズエラー
	public final static int ERR_COURPUS_CLOSE = 121;
	//インデックスオープンエラー(eix)
	public final static int ERR_INDEX_OPEN_EIX = 211;
	//インデックスオープンエラー(その他)
	public final static int ERR_INDEX_OPEN_OTHER = 212;
	//インデックスクローズエラー
	public final static int ERR_INDEX_CLOSE = 221;
	//検索語未入力エラー
	public final static int ERR_GUI_INPUT_NULL = 331;
	//検索語バリデーションエラー
	public final static int ERR_GUI_INPUT_INVALID = 332;
	//その他エラー
	public final static int ERR_OTHER = 999;


	/************************************
	 * メンバ変数
	 ************************************/
	//対象ファイル名
	private String fileName ;
	//エラーコード
	private int errorCode;
	//throwableオブジェクト
	private Throwable throwable;
	//エラー発生行
	private int lineNo = -1;
	//エラー発生クラス
	private String className = ""; //$NON-NLS-1$


	/************************************
	 * メソッド
	 ************************************/

	/**
	 * コンストラクタ
	 */
	ErrorMsg(Throwable throwable,int errorCode, String fileName){
		this.throwable = throwable;
		this.errorCode = errorCode;
		this.fileName = fileName;
		init();
	}

	ErrorMsg(Throwable throwable){
		this.fileName = null;
		this.errorCode = ERR_OTHER;
		this.throwable = throwable;
		init();
	}

	/**
	 * エラーメッセージを取得する
	 * @return エラーメッセージ
	 */
	public String getMessage() {

		String msg = ""; //$NON-NLS-1$
		String reason = getReason();
		String detail = getDetail();
		msg = reason + "\n" + detail; //$NON-NLS-1$

		return msg;
	}


	/**
	 * エラー理由の文字列を取得する
	 * @return String エラー理由
	 */
	private String getReason() {

		String ret = ""; //$NON-NLS-1$
		String msgId = ""; //$NON-NLS-1$

		//エラーコードに対応するメッセージコードを取得する
		switch(errorCode){
		case ERR_COURPUS_OPEN: msgId = "SearchEngine.52"; break; //$NON-NLS-1$
		case ERR_COURPUS_CLOSE: msgId = "SearchEngine.137"; break; //$NON-NLS-1$
		case ERR_INDEX_OPEN_EIX: msgId = "SearchEngine.134"; break; //$NON-NLS-1$
		case ERR_INDEX_OPEN_OTHER: msgId = "SearchEngine.134"; break; //$NON-NLS-1$
		case ERR_INDEX_CLOSE: msgId = "SearchEngine.135"; break; //$NON-NLS-1$
		case ERR_GUI_INPUT_NULL: msgId = "SearchEngine.51"; break; //$NON-NLS-1$
		case ERR_GUI_INPUT_INVALID: msgId = "SearchEngine.60"; break; //$NON-NLS-1$
		default: msgId = "Frame1.521"; break; //$NON-NLS-1$
		}
		ret = Messages.getString(msgId);

		//その他の理由の場合、throwableからメッセージを取得し()付けで表示
		//メッセージが取得できない場合、原因不明エラーとする
		if(errorCode == ERR_OTHER ){
			if(throwable.getMessage() != null){
				ret += "("+ throwable.getMessage()+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return ret;
	}

	/**
	 * エラー詳細を取得する
	 * @return String エラー詳細
	 */
	private String getDetail() {

		String ret = "  " + Messages.getString("Frame1.518") + errorCode + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(fileName != null){
			ret += "  " + Messages.getString("Frame1.519") + fileName + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if(lineNo > 0){
		    ret += "  " + Messages.getString("Frame1.520") + " " + className + ": " + lineNo + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}

		return ret;
	}



	/**
	 * オブジェクトの初期化
	 */
	private void init(){

		//エラー情報（エラー発生クラス・行）の取得
		getErrorInfo();

	}



	/**
	 * エラー情報（エラークラス、エラー行）を取得する
	 */
	private void getErrorInfo(){
		// 例外オブジェクトからスタックトレース情報を取得
		StackTraceElement[] stackTrace = throwable.getStackTrace();

		// スタックトレース情報からクラス名と一致する情報を取得する
		for(int i = 0; i < stackTrace.length; i++)
		{
			//行番号が取得できる最初のstackの情報を表示
			if(stackTrace[i].getLineNumber() > 0)
			{
				// 行番号を取得する
				lineNo = stackTrace[i].getLineNumber();
				className = stackTrace[i].getClassName();
				break;
			}
		}
	}


	/**
	 * エラーダイアログを表示する
	 * @param throwable  throwableオブジェクト
	 */
	public void showErrorDialog(){
        try {
        	throwable.printStackTrace();

            // EDT以外で呼ばれる事も想定し、EDTで実行する。
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	ErrorMsg error = new ErrorMsg(throwable);
                    JOptionPane.showMessageDialog(
                            null,
                            error.getMessage(),
                            Messages.getString("Frame1.209"), //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }catch (OutOfMemoryError e){
        	//メモリ以上の場合はシステム終了
        	System.exit(-1);

		}catch (Throwable e) {
            // ダイアログ表示時はエラーは全て握りつぶす
        }
	}


}
