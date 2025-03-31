<?xml version="1.0" encoding="SHIFT_JIS"?>
<!--
  kotobun_written.xsl
  Written by Sae UENO, Miwa FUJIMOTO, Masaya YAMAGUCHI, 2007-04-23
  Copyright 2007 The National Institute for Japanese Language
  This script is distributed under GNU Public License.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- 表示用 -->

  <xsl:template match="記事">
    <html xmlns:xhtml="http://www.w3.org/1999/xhtml">
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=shift_jis"/>
        <meta http-equiv="Content-Style-Type" content="text/css" />
        <link rel="stylesheet" href="kotobun_written.css" type="text/css" />
	<title>
		<xsl:value-of select="@タイトル"/>
		<xsl:text>：</xsl:text>
		<xsl:value-of select="@著者"/>
	</title>
      </head>

      <body>
        <h1>
          <xsl:value-of select="@タイトル" />
          <xsl:text>：</xsl:text>
          <xsl:value-of select="@著者" />
        </h1>
        <h2>
          <xsl:value-of select="@底本" />
        </h2>
        <xsl:apply-templates/>
      </body>
    </html>
</xsl:template>

<xsl:template match="階層">
  <xsl:choose>
    <xsl:when test="@種別='h1'">
      <hr style="border-style:solid; background-color:lemonchiffon; border:none; height:10px;"/>
      <xsl:apply-templates/>
      <br/><hr style="border-style:solid; background-color:lemonchiffon; border:none; height:10px;"/>
    </xsl:when>
    <xsl:when test="@種別='h2'">
      <xsl:apply-templates/>
      <br/><hr style="border-style:solid; background-color:lemonchiffon; border:none; height:5px;"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/><br/><hr style="border-style:solid; background-color:lemonchiffon; border:none; height:3px;"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="引用">
	<xsl:choose>
		<xsl:when test="@種別='和歌'">
			<span title="{@種別}：{@引用元}" style="background-color:mistyrose; cursor:pointer; "><em><xsl:apply-templates/></em></span>
		</xsl:when>
		<xsl:otherwise>
		<span title="{@種別}：{@引用元}" style="background-color:mistyrose; cursor:pointer;"><xsl:apply-templates/></span>
		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>

  <xsl:template match="タイトル">
    <h3><xsl:apply-templates/></h3>
  </xsl:template>

  <xsl:template match="著者">
    <b><xsl:apply-templates/></b>
  </xsl:template>


  <xsl:template match="話者">
    <xsl:text>　</xsl:text>
    <em><xsl:apply-templates/></em>
    <xsl:text>　</xsl:text>
  </xsl:template>

  <xsl:template match="ブロック">
    <xsl:choose>
      <xsl:when test="@種別='ト書き'">
    <span style="font-size: x-small; "><xsl:apply-templates/></span>
      </xsl:when>
      <xsl:when test="@種別='詞書'">
    <span title="詞書"><xsl:apply-templates/></span><br />
      </xsl:when>
      <xsl:when test="@種別='左注'">
    <span title="左注"><xsl:apply-templates/></span>
      </xsl:when>

      <xsl:when test="@種別='字下げ'">
    <span title="{@種別}" style="cursor: pointer"><p style="padding-left:2em;"><xsl:apply-templates/></p></span>
      </xsl:when>
      <xsl:when test="@種別='ぶら下げ'">
    <span title="{@種別}" style="cursor: pointer" ><p style="padding:0px 0px 0px 2em; "><xsl:apply-templates/></p></span>
      </xsl:when>
      <xsl:when test="@種別='地付き'">
    <span title="{@種別}" style="cursor: pointer"><p style="text-align:right; margin-right:2em"><xsl:apply-templates/></p></span>
      </xsl:when>


      <xsl:otherwise>
    <br /><span style="border-style:solid; border-width:thin; border-color:gainsboro; display:block; "><xsl:apply-templates/></span><br />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="小書">
    <xsl:choose>
      <xsl:when test="span">
        <xsl:apply-templates/>
      </xsl:when>
    <xsl:otherwise>
    <sub><xsl:apply-templates/></sub>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="span">
    <xsl:choose>
      <xsl:when test="@種別='傍点'">
        <span title="{@種別}{@備考}" style="cursor:pointer; background-color:azure;"><xsl:apply-templates/></span>
      </xsl:when>

      <xsl:when test="@種別='傍線'">
        <span title="傍線" style="cursor:pointer; text-decoration:underline"><xsl:apply-templates/></span>
      </xsl:when>

      <xsl:when test="@備考='上付き'">
        <span title="{@備考}" style="cursor:pointer; background-color:azure;"><sup><xsl:apply-templates/></sup></span>
      </xsl:when>

      <xsl:when test="@備考='下付き'">
        <span title="{@備考}" style="cursor:pointer; background-color:azure;"><sub><xsl:apply-templates/></sub></span>
      </xsl:when>

      <xsl:when test="@種別='空白'">
        <xsl:text>　</xsl:text>
      </xsl:when>

      <xsl:otherwise>
        <span title="{@種別}" style="cursor:pointer;"><strong><xsl:apply-templates/></strong></span>
      </xsl:otherwise>

    </xsl:choose>
  </xsl:template>

  <xsl:template match="ヘッダ">
    <span style="color:lightsteelblue">
      <xsl:apply-templates/>
    </span>
    <hr/>
  </xsl:template>

  <xsl:template match="フッタ">
    <span style="color:lightsteelblue">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="注">
    <xsl:choose>
      <xsl:when test="@種別='ママ'">
    <xsl:apply-templates />
    <sup style="color:red">
        <xsl:text>（ママ）</xsl:text>
    </sup>
      </xsl:when>


      <xsl:when test="@付与='原文'">
         <span title="{@種別}：{@付与}" style="font-size: x-small; cursor:pointer;"><xsl:apply-templates /></span>
      </xsl:when>

      <xsl:when test="@内容">
         <span title="{@種別}：{@付与}：{@備考}" style="font-size: x-small; color: mediumseagreen; cursor:pointer;"><xsl:value-of select="@内容" /></span>
      </xsl:when>

      <xsl:when test="@備考">
        <xsl:call-template name="notes"/>
      </xsl:when>

      <xsl:otherwise>
      <span title="{@種別}：{@付与}" style="cursor:pointer; color:lightsteelblue;"><xsl:apply-templates /></span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


<xsl:template name="notes">
<xsl:choose>
  <xsl:when test="@備考=contains(@備考,'傍記')">
         <br /><span title="{@種別}：{@付与}：{@備考}" style="font-size: small; cursor:pointer;"><xsl:apply-templates /></span>
  </xsl:when>
  <xsl:otherwise>
         <span title="{@種別}：{@付与}：{@備考}" style="font-size: small; cursor:pointer;"><xsl:apply-templates /></span>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>


  <xsl:template match="欠損">
    <xsl:apply-templates/>
      <sup title="{@種別}" style="color:red;cursor:pointer">※</sup>
  </xsl:template>

  <xsl:template match="r">
    <ruby>
        <rb><xsl:apply-templates/></rb>
	<rp><xsl:text>(</xsl:text></rp>
        <rt><xsl:value-of select="@rt"/></rt>
	<rp><xsl:text>)</xsl:text></rp>
    </ruby>
  <xsl:if test="@lrt">
        <span style="color:indigo">
        <xsl:text>（</xsl:text>
        <xsl:value-of select="@lrt"/>
        <xsl:text>）</xsl:text>
        </span>
  </xsl:if>
  </xsl:template>

<xsl:template match="割書">
    <span style="font-size: x-small;">［<xsl:apply-templates/>］</span>
</xsl:template>
<xsl:template match="割書改行">
<span class="gray">／</span>
</xsl:template>

  <xsl:template match="画像">
  <xsl:choose>
    <xsl:when test="@備考='gaiji'">
    <img>
      <xsl:attribute name="src"><xsl:value-of select="@src" /></xsl:attribute>
      <xsl:attribute name="alt"><xsl:value-of select="@alt" /></xsl:attribute>
  <xsl:if test="@height">
      <xsl:attribute name="height"><xsl:value-of select="@height" /></xsl:attribute>
  </xsl:if>
  <xsl:if test="@width">
      <xsl:attribute name="width"><xsl:value-of select="@width" /></xsl:attribute>
  </xsl:if>
   </img>
  </xsl:when>
  <xsl:otherwise>
    <br/><img>
      <xsl:attribute name="src"><xsl:value-of select="@src" /></xsl:attribute>
      <xsl:attribute name="alt"><xsl:value-of select="@alt" /></xsl:attribute>
  <xsl:if test="@height">
      <xsl:attribute name="height"><xsl:value-of select="@height" /></xsl:attribute>
  </xsl:if>
  <xsl:if test="@width">
      <xsl:attribute name="width"><xsl:value-of select="@width" /></xsl:attribute>
  </xsl:if>
   </img><br />
    </xsl:otherwise>
  </xsl:choose>
  </xsl:template>

  <xsl:template match="いおり点">　√</xsl:template>

  <xsl:template match="外字">
    <span title="{@備考}" style="cursor:pointer"><xsl:apply-templates/></span>
  </xsl:template>

  <xsl:template match="印">
    <span style="color:darkred">［<xsl:value-of select="@内容"/>］</span>
  </xsl:template>

  <xsl:template match="頁"><p style="color:silver">(p.<xsl:value-of select="@no"/>)</p>
  </xsl:template>

  <xsl:template match="行">
  <xsl:choose>
    <xsl:when test="@no">
    <span style="color:silver; text-align:right" >(<xsl:value-of select="@no"/>)</span><br />
    </xsl:when>
    <xsl:otherwise>
      <br />
    </xsl:otherwise>
  </xsl:choose>
  </xsl:template>

  <xsl:template match="b"><br/></xsl:template>

<!-- nijl追加 -->

<xsl:template match="詞書">
    <span title="詞書"><xsl:apply-templates/></span><br />
</xsl:template>

<xsl:template match="左注">
    <br /><span title="左注"><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match="セクション">
    <xsl:apply-templates/>
    <p style="color:silver; text-align: center; "><xsl:text>No.</xsl:text><xsl:value-of select="@id"/></p><br />
</xsl:template>

<xsl:template match="sp">
  <xsl:apply-templates/>
</xsl:template>

  <xsl:template match="tg">
    <span class="target_char">
      <xsl:attribute name="id">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </span>
  </xsl:template>


  <xsl:template match="text()|@*">
    <xsl:call-template name="replace">
      <xsl:with-param name="str" select="." />
    </xsl:call-template>
  </xsl:template>


  <!--
     Refered from
     http://www.geocities.co.jp/SiliconValley-Oakland/8718/faqxslt/replace2.xsl.txt
  -->
  <xsl:template name="replace">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="contains($str,'&#10;')">
<!-- str：数値を文字列に変換　$strに&#10;（改行）が含まれるかどうか -->
        <xsl:value-of select="substring-before($str,'&#10;')"/><br />
<!-- substring-before：&#10;の前の文字列を取り出す -->
        <xsl:call-template name="replace">
          <xsl:with-param name="str" select="substring-after($str,'&#10;')"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
