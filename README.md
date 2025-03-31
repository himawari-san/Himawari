# 全文検索システム『ひまわり』
『ひまわり』は，言語研究用に設計された全文検索システムで，次の機能を持っています。

- XML 文書から特定の文字列を高速に全文検索する機能 (Unicode に対応)
- 検索結果の KWIC (KeyWord In Context) 表示，および，資料に適した形で閲覧する機能

『ひまわり』を使うことにより，『太陽コーパス』，『日本語話し言葉コーパス』，『分類語彙表』などの既存の言語資料や，自分で作成した XML 文書を検索することができるようになります。なお，『ひまわり』は Java 言語で記述されており，Windows, Linux, macOS などさまざまな OS 上で動作します。

---

## データの概要
- 本リポジトリでは，『ひまわり』ver.1.8以降のソースファイルを公開しています。
- 『ひまわり』ver.1.8以降の開発は，本リポジトリ上で行います。ver.1.8のブランチ（現在はmain）の初期状態は，機能的には安定版のver.1.7.4と同一，ソースとしても大きな違いはありません（ファイル構成の変更や一部ライブラリを自前のコードに置き換えするなどしています）。
- 『ひまわり』の安定版やマニュアルの配布は，国語研究所の[『ひまわり』のホームページ](https://csd.ninjal.ac.jp/lrc/?%C1%B4%CA%B8%B8%A1%BA%F7%A5%B7%A5%B9%A5%C6%A5%E0%A1%D8%A4%D2%A4%DE%A4%EF%A4%EA%A1%D9)を参照してください。

## 動作環境と依存ライブラリ
- Java 17
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/)
- [Apache Commons Lang](https://commons.apache.org/proper/commons-lang/)
- [H2 Database](https://www.h2database.com/)
- [juniversalchardet](https://code.google.com/archive/p/juniversalchardet/)
- [vorbis-java-core](https://github.com/gagravarr/vorbis-java)

## ビルド

```sh
git clone https://github.com/himawari-san/Himawari
mvn clean package
```

## 実行方法

1. target/himawari-version.zip を適当な場所に展開してください。
2. 『ひまわり』のフォルダに移動します。フォルダ名はバージョンによって異なります。
```
cd himawari-1_8a_20250331
```
3. コマンドラインから次のコマンドを実行します。別途JREをインストールしておくことが必要です。
```sh
java -jar himawari.jar 
```

## ライセンス
[GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.en.html)

