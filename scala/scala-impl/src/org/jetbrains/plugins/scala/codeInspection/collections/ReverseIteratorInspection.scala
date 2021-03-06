package org.jetbrains.plugins.scala
package codeInspection
package collections

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression

import scala.collection.immutable.ArraySeq

/**
 * @author Nikolay.Tropin
 */
object ReverseIterator extends SimplificationType {
  override def hint: String = ScalaInspectionBundle.message("replace.reverse.iterator")

  override def getSimplification(expr: ScExpression): Option[Simplification] = {
    expr match {
      case qual`.reverse`Seq()`.iterator`Seq() =>
        Some(replace(expr).withText(invocationText(qual, "reverseIterator")).highlightFrom(qual))
      case _ => None
    }
  }
}

class ReverseIteratorInspection extends OperationOnCollectionInspection {
  override def possibleSimplificationTypes: ArraySeq[SimplificationType] = ArraySeq(ReverseIterator)
}
