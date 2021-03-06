package scalariform.lexer

import scala.collection.mutable.ListBuffer
import scalariform.lexer.Tokens._

class WhitespaceAndCommentsGrouper(lexer: ScalaLexer) extends Iterator[Token] {

  private var nextToken = lexer.next()

  private var ended = false

  private var hiddenTokens: HiddenTokens = _

  def getHiddenTokens: HiddenTokens = hiddenTokens

  def hasNext: Boolean = !ended

  private[lexer] def text = lexer.text

  def next(): Token = {
    require(hasNext)
    hiddenTokens = readHiddenTokens()
    val resultToken = nextToken
    resultToken.associatedWhitespaceAndComments_ = hiddenTokens
    if (nextToken.tokenType == EOF)
      ended = true
    nextToken = lexer.next()
    resultToken
  }

  private def readHiddenTokens(): HiddenTokens = {
    val hiddenTokens = new ListBuffer[HiddenToken]
    while (isCommentOrWhitespace(nextToken)) {
      hiddenTokens += makeHiddenToken(nextToken)
      nextToken = lexer.next()
    }
    HiddenTokens(hiddenTokens.toList)
  }

  private def isCommentOrWhitespace(token: Token) = token.tokenType match {
    case WS | LINE_COMMENT | MULTILINE_COMMENT ⇒ true
    case _                                     ⇒ false
  }

  private def makeHiddenToken(token: Token) = token.tokenType match {
    case LINE_COMMENT ⇒ SingleLineComment(token)
    case MULTILINE_COMMENT if token.isScalaDocComment ⇒ ScalaDocComment(token)
    case MULTILINE_COMMENT ⇒ MultiLineComment(token)
    case WS ⇒ Whitespace(token)
  }

}
