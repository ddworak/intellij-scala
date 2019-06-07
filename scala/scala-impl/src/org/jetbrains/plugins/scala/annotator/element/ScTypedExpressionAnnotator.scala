package org.jetbrains.plugins.scala.annotator.element

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import org.jetbrains.plugins.scala.annotator.TypeDiff
import org.jetbrains.plugins.scala.annotator.TypeDiff.Mismatch
import org.jetbrains.plugins.scala.annotator.quickfix.ReportHighlightingErrorQuickFix
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScTypedExpression}
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.extensions._

object ScTypedExpressionAnnotator extends ElementAnnotator[ScTypedExpression] {
  override def annotate(element: ScTypedExpression, holder: AnnotationHolder, typeAware: Boolean): Unit = {
    if (typeAware) {
      element.typeElement.foreach(checkUpcasting(element.expr, _, holder))
    }
  }

  // SCL-15544
  private def checkUpcasting(expression: ScExpression, typeElement: ScTypeElement, holder: AnnotationHolder): Unit = {
    expression.getTypeAfterImplicitConversion().tr.foreach { actual =>
      val expected = typeElement.calcType

      if (!actual.conforms(expected)) {
        val ranges = mismatchRangesIn(typeElement, expected, actual)
        // TODO add messange to the whole element, but higlight separate parts?
        // TODO fine-grained tooltip
        val message = s"Cannot upcast ${actual.presentableText} to ${expected.presentableText}"
        ranges.foreach { range =>
          val annotation = holder.createErrorAnnotation(range, message)
          annotation.registerFix(ReportHighlightingErrorQuickFix)
        }
      }
    }
  }

  // SCL-15481
  def mismatchRangesIn(typeAscription: ScTypeElement, expected: ScType, actual: ScType): Seq[TextRange] = {
    val diff = TypeDiff.forExpected(expected, actual)

    if (diff.text != typeAscription.getText) { // make sure that presentations match
      val (ranges, _) =  diff.flatten.foldLeft((Vector.empty[TextRange], typeAscription.getTextOffset)) { case ((acc, offset), x) =>
        val length = x.text.length
        (if (x.is[Mismatch]) acc :+ TextRange.create(offset, offset + length) else acc, offset + length)
      }
      ranges
    } else {
      Seq(typeAscription.getTextRange)
    }
  }
}
