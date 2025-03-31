<?xml version="1.0" encoding="shift_jis" ?>

<!--
  aozora.xsl
  Written by Miwa FUJIMOTO, Sae UENO, Masaya YAMAGUCHI, 2007-04-23
  Copyright 2007 The National Institute for Japanese Language
  This script is distributed under GNU Public License.
-->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xhtml"
  version="1.0">
<xsl:output method="xml" omit-xml-declaration="yes"/>

<!-- 全体 -->
<xsl:template match="/">
  <記事>
    <xsl:attribute name="タイトル">
      <xsl:value-of select="//xhtml:h1[@class='title']"/>
    </xsl:attribute>

    <xsl:attribute name="サブタイトル">
      <xsl:value-of select="//xhtml:h2[@class='subtitle']"/>
    </xsl:attribute>
    
    <xsl:attribute name="著者">
      <xsl:if test="//xhtml:h2/@class='author'">
	<xsl:value-of select="//xhtml:h2[@class='author']"/>
      </xsl:if>
    </xsl:attribute>
    
    <xsl:attribute name="底本" />
    <xsl:apply-templates />
  </記事>
</xsl:template>

<!-- ヘッダ -->
<xsl:template match="xhtml:head">
  <ヘッダ>

<xsl:text>タイトル：</xsl:text>
   <xsl:value-of select="//xhtml:h1[@class='title']"/>
 <b />

<xsl:text>サブタイトル：</xsl:text>
  <xsl:if test="//xhtml:h2[@class='subtitle']">
   <xsl:value-of select="//xhtml:h2[@class='subtitle']"/>
  </xsl:if>
 <b />

<xsl:text>原題：</xsl:text>
  <xsl:if test="//xhtml:h2[@class='original_title']">
   <xsl:value-of select="//xhtml:h2[@class='original_title']"/>
  </xsl:if>
      <b />

<xsl:text>著者：</xsl:text>
  <xsl:if test="//xhtml:h2[@class='author']">
   <xsl:value-of select="//xhtml:h2[@class='author']"/>
  </xsl:if>
 <b />

<xsl:text>翻訳者：</xsl:text>
  <xsl:if test="//xhtml:h2[@class='translator']">
   <xsl:value-of select="//xhtml:h2[@class='translator']"/>
  </xsl:if>
 <b />

<xsl:text>編者：</xsl:text>
  <xsl:if test="//xhtml:h2[@class='editor']">
   <xsl:value-of select="//xhtml:h2[@class='editor']"/>
  </xsl:if>
 <b />

  </ヘッダ>
</xsl:template>

<!-- タイトル部分 -->
<xsl:template match="xhtml:h1">
      <タイトル><xsl:apply-templates/></タイトル>
</xsl:template>

<xsl:template match="xhtml:h2">
   <xsl:choose>

    <xsl:when test="@class='subtitle'">
      <タイトル 備考="サブタイトル"><xsl:apply-templates/></タイトル>
    </xsl:when>

    <xsl:when test="@class='original_title'">
      <タイトル 備考="原題"><xsl:apply-templates/></タイトル>
    </xsl:when>

    <xsl:when test="@class='author'">
      <著者><xsl:apply-templates/></著者>
    </xsl:when>

    <xsl:when test="@class='translator'">
      <著者 備考="翻訳者"><xsl:apply-templates/></著者>
    </xsl:when>

    <xsl:when test="@class='editor'">
      <著者 備考="編者"><xsl:apply-templates/></著者>
    </xsl:when>

   </xsl:choose>
</xsl:template>

<!-- ＊＊＊div ココカラ＊＊＊ -->

<!-- 本文 -->
<xsl:template match="xhtml:div[@class='main_text']">
  <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>テキスト 種別="本文"</xsl:text><xsl:text disable-output-escaping="yes">&gt;
   &lt;</xsl:text><xsl:text>階層 種別="h1" タイトル="</xsl:text>
     <xsl:value-of select="//xhtml:h1[@class='title']"/>
   <xsl:text>"</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

       <xsl:apply-templates/>

    <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>/階層</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

  <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>/テキスト</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

  </xsl:template>

<!-- インデント系 -->
<xsl:template match="xhtml:div[starts-with(@class,'chitsuki_')]">
   <ブロック 種別="地付き">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </ブロック>
</xsl:template>

<xsl:template match="xhtml:div[starts-with(@class,'jisage_')]">
   <ブロック 種別="字下げ">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </ブロック>
</xsl:template>

<xsl:template match="xhtml:div[@class='burasage']">
   <ブロック 種別="ぶら下げ">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </ブロック>
</xsl:template>

<!-- フッタ -->
<xsl:template match="xhtml:div[@class='bibliographical_information']">
  <フッタ>
    <ブロック 種別="書誌情報">
      <xsl:apply-templates/>
    </ブロック>
    <ブロック 種別="表記について">
      <xsl:apply-templates select="../xhtml:div[@class='notation_notes']" mode="call" />
    </ブロック>
    <ブロック 種別="図書カード">
      <xsl:apply-templates select="../xhtml:div[@class='card']" mode="call" />
    </ブロック>
    <ブロック 種別="変換者情報">
<xsl:text>
This document was transformed to HIMAWARI format from the original document.
The transformation script was written by Masaya YAMAGUCHI, Sae UENO and Miwa FUJIMOTO.
 </xsl:text>
    </ブロック>
  </フッタ>
</xsl:template>

<xsl:template match="xhtml:div[@class='card']" mode="call" >
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="xhtml:div[@class='notation_notes']" >
</xsl:template>


<xsl:template match="xhtml:div[@class='notation_notes']" mode="call" >
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="xhtml:div[@class='notation_notes']" >
</xsl:template>

<!-- ＊＊＊div ココマデ＊＊＊ -->

<!-- 傍点 -->
<xsl:template match="xhtml:strong">
  <span 種別="傍点">
    <xsl:attribute name="備考">
      <xsl:value-of select="@class"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- 傍線 -->
<xsl:template match="xhtml:em">
  <span 種別="傍線">
    <xsl:attribute name="備考">
      <xsl:value-of select="@class"/>
    </xsl:attribute>
    <xsl:attribute name="style">
      <xsl:value-of select="@style"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- 太字 -->
<xsl:template match="xhtml:b">
  <span 種別="強調"><xsl:apply-templates/></span>
</xsl:template>

<!-- 小書 -->
<xsl:template match="xhtml:small">
  <小書><xsl:apply-templates/></小書>
</xsl:template>

<!-- 上付き・下付き -->
<xsl:template match="xhtml:sup">
  <span 種別="強調" 備考="上付き"><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match="xhtml:sub">
  <span 種別="強調" 備考="下付き"><xsl:apply-templates/></span>
</xsl:template>

<!-- 改行 -->
<xsl:template match="xhtml:br">
  <b />
</xsl:template>

<!-- ルビ -->
<xsl:template match="xhtml:ruby">
   <xsl:apply-templates/>
</xsl:template>

<xsl:template match="xhtml:rb">
  <r>
    <xsl:attribute name="rt">
      <xsl:value-of select="../xhtml:rt"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </r>
</xsl:template>

<xsl:template match="xhtml:rt">
</xsl:template>

<xsl:template match="xhtml:rp">
</xsl:template>

<!-- 画像 -->
<xsl:template match="xhtml:img">
  <画像>

 <xsl:if test="@class='illustration'">
   <xsl:attribute name="src">
     <xsl:value-of select="concat('illustration/',@src)"/>
   </xsl:attribute>

   <xsl:attribute name="height">
     <xsl:value-of select="@height"/>
   </xsl:attribute>

   <xsl:attribute name="width">
     <xsl:value-of select="@width"/>
   </xsl:attribute>

 </xsl:if>

 <xsl:if test="@class='gaiji'">
   <xsl:attribute name="src">
     <xsl:value-of select="substring-after(@src,'../../../')"/>
   </xsl:attribute>
 </xsl:if>

   <xsl:attribute name="alt">
     <xsl:value-of select="@alt"/>
   </xsl:attribute>

   <xsl:attribute name="備考">
     <xsl:value-of select="@class"/>
   </xsl:attribute>

  </画像>

</xsl:template>

<!-- 注 -->
<xsl:template match="xhtml:span">
<xsl:choose>


  <xsl:when test="@class='notes'">
   <注 種別="注記" 付与="入力">
   <xsl:attribute name="内容">
     <xsl:value-of select="string(.)" />
   </xsl:attribute>
   </注>
  </xsl:when>

  <xsl:when test="@class='warichu'">
   <割書>
     <xsl:apply-templates/>
   </割書>
  </xsl:when>

  <xsl:otherwise>
   <span 種別="属性なし">
     <xsl:apply-templates/>
   </span>
  </xsl:otherwise>

</xsl:choose>
</xsl:template>



<!-- リスト・テーブル -->
<xsl:template match="xhtml:li">
  <xsl:apply-templates/><b />
</xsl:template>

<xsl:template match="xhtml:tr">
  <xsl:apply-templates/><b />
</xsl:template>

<xsl:template match="xhtml:td">
  <xsl:apply-templates/>　
</xsl:template>


</xsl:stylesheet>
