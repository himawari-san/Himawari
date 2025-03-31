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

<!-- �S�� -->
<xsl:template match="/">
  <�L��>
    <xsl:attribute name="�^�C�g��">
      <xsl:value-of select="//xhtml:h1[@class='title']"/>
    </xsl:attribute>

    <xsl:attribute name="�T�u�^�C�g��">
      <xsl:value-of select="//xhtml:h2[@class='subtitle']"/>
    </xsl:attribute>
    
    <xsl:attribute name="����">
      <xsl:if test="//xhtml:h2/@class='author'">
	<xsl:value-of select="//xhtml:h2[@class='author']"/>
      </xsl:if>
    </xsl:attribute>
    
    <xsl:attribute name="��{" />
    <xsl:apply-templates />
  </�L��>
</xsl:template>

<!-- �w�b�_ -->
<xsl:template match="xhtml:head">
  <�w�b�_>

<xsl:text>�^�C�g���F</xsl:text>
   <xsl:value-of select="//xhtml:h1[@class='title']"/>
 <b />

<xsl:text>�T�u�^�C�g���F</xsl:text>
  <xsl:if test="//xhtml:h2[@class='subtitle']">
   <xsl:value-of select="//xhtml:h2[@class='subtitle']"/>
  </xsl:if>
 <b />

<xsl:text>����F</xsl:text>
  <xsl:if test="//xhtml:h2[@class='original_title']">
   <xsl:value-of select="//xhtml:h2[@class='original_title']"/>
  </xsl:if>
      <b />

<xsl:text>���ҁF</xsl:text>
  <xsl:if test="//xhtml:h2[@class='author']">
   <xsl:value-of select="//xhtml:h2[@class='author']"/>
  </xsl:if>
 <b />

<xsl:text>�|��ҁF</xsl:text>
  <xsl:if test="//xhtml:h2[@class='translator']">
   <xsl:value-of select="//xhtml:h2[@class='translator']"/>
  </xsl:if>
 <b />

<xsl:text>�ҎҁF</xsl:text>
  <xsl:if test="//xhtml:h2[@class='editor']">
   <xsl:value-of select="//xhtml:h2[@class='editor']"/>
  </xsl:if>
 <b />

  </�w�b�_>
</xsl:template>

<!-- �^�C�g������ -->
<xsl:template match="xhtml:h1">
      <�^�C�g��><xsl:apply-templates/></�^�C�g��>
</xsl:template>

<xsl:template match="xhtml:h2">
   <xsl:choose>

    <xsl:when test="@class='subtitle'">
      <�^�C�g�� ���l="�T�u�^�C�g��"><xsl:apply-templates/></�^�C�g��>
    </xsl:when>

    <xsl:when test="@class='original_title'">
      <�^�C�g�� ���l="����"><xsl:apply-templates/></�^�C�g��>
    </xsl:when>

    <xsl:when test="@class='author'">
      <����><xsl:apply-templates/></����>
    </xsl:when>

    <xsl:when test="@class='translator'">
      <���� ���l="�|���"><xsl:apply-templates/></����>
    </xsl:when>

    <xsl:when test="@class='editor'">
      <���� ���l="�Ҏ�"><xsl:apply-templates/></����>
    </xsl:when>

   </xsl:choose>
</xsl:template>

<!-- ������div �R�R�J�������� -->

<!-- �{�� -->
<xsl:template match="xhtml:div[@class='main_text']">
  <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>�e�L�X�g ���="�{��"</xsl:text><xsl:text disable-output-escaping="yes">&gt;
   &lt;</xsl:text><xsl:text>�K�w ���="h1" �^�C�g��="</xsl:text>
     <xsl:value-of select="//xhtml:h1[@class='title']"/>
   <xsl:text>"</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

       <xsl:apply-templates/>

    <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>/�K�w</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

  <xsl:text disable-output-escaping="yes">&lt;</xsl:text><xsl:text>/�e�L�X�g</xsl:text><xsl:text disable-output-escaping="yes">&gt;</xsl:text>

  </xsl:template>

<!-- �C���f���g�n -->
<xsl:template match="xhtml:div[starts-with(@class,'chitsuki_')]">
   <�u���b�N ���="�n�t��">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </�u���b�N>
</xsl:template>

<xsl:template match="xhtml:div[starts-with(@class,'jisage_')]">
   <�u���b�N ���="������">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </�u���b�N>
</xsl:template>

<xsl:template match="xhtml:div[@class='burasage']">
   <�u���b�N ���="�Ԃ牺��">
     <xsl:attribute name="style">
         <xsl:value-of select="@style"/>
     </xsl:attribute>
   <xsl:apply-templates/>
   </�u���b�N>
</xsl:template>

<!-- �t�b�^ -->
<xsl:template match="xhtml:div[@class='bibliographical_information']">
  <�t�b�^>
    <�u���b�N ���="�������">
      <xsl:apply-templates/>
    </�u���b�N>
    <�u���b�N ���="�\�L�ɂ���">
      <xsl:apply-templates select="../xhtml:div[@class='notation_notes']" mode="call" />
    </�u���b�N>
    <�u���b�N ���="�}���J�[�h">
      <xsl:apply-templates select="../xhtml:div[@class='card']" mode="call" />
    </�u���b�N>
    <�u���b�N ���="�ϊ��ҏ��">
<xsl:text>
This document was transformed to HIMAWARI format from the original document.
The transformation script was written by Masaya YAMAGUCHI, Sae UENO and Miwa FUJIMOTO.
 </xsl:text>
    </�u���b�N>
  </�t�b�^>
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

<!-- ������div �R�R�}�f������ -->

<!-- �T�_ -->
<xsl:template match="xhtml:strong">
  <span ���="�T�_">
    <xsl:attribute name="���l">
      <xsl:value-of select="@class"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- �T�� -->
<xsl:template match="xhtml:em">
  <span ���="�T��">
    <xsl:attribute name="���l">
      <xsl:value-of select="@class"/>
    </xsl:attribute>
    <xsl:attribute name="style">
      <xsl:value-of select="@style"/>
    </xsl:attribute>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- ���� -->
<xsl:template match="xhtml:b">
  <span ���="����"><xsl:apply-templates/></span>
</xsl:template>

<!-- ���� -->
<xsl:template match="xhtml:small">
  <����><xsl:apply-templates/></����>
</xsl:template>

<!-- ��t���E���t�� -->
<xsl:template match="xhtml:sup">
  <span ���="����" ���l="��t��"><xsl:apply-templates/></span>
</xsl:template>

<xsl:template match="xhtml:sub">
  <span ���="����" ���l="���t��"><xsl:apply-templates/></span>
</xsl:template>

<!-- ���s -->
<xsl:template match="xhtml:br">
  <b />
</xsl:template>

<!-- ���r -->
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

<!-- �摜 -->
<xsl:template match="xhtml:img">
  <�摜>

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

   <xsl:attribute name="���l">
     <xsl:value-of select="@class"/>
   </xsl:attribute>

  </�摜>

</xsl:template>

<!-- �� -->
<xsl:template match="xhtml:span">
<xsl:choose>


  <xsl:when test="@class='notes'">
   <�� ���="���L" �t�^="����">
   <xsl:attribute name="���e">
     <xsl:value-of select="string(.)" />
   </xsl:attribute>
   </��>
  </xsl:when>

  <xsl:when test="@class='warichu'">
   <����>
     <xsl:apply-templates/>
   </����>
  </xsl:when>

  <xsl:otherwise>
   <span ���="�����Ȃ�">
     <xsl:apply-templates/>
   </span>
  </xsl:otherwise>

</xsl:choose>
</xsl:template>



<!-- ���X�g�E�e�[�u�� -->
<xsl:template match="xhtml:li">
  <xsl:apply-templates/><b />
</xsl:template>

<xsl:template match="xhtml:tr">
  <xsl:apply-templates/><b />
</xsl:template>

<xsl:template match="xhtml:td">
  <xsl:apply-templates/>�@
</xsl:template>


</xsl:stylesheet>
