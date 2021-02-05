package org.jetbrains.plugins.scala

import com.intellij.testFramework.UsefulTestCase
import org.jetbrains.plugins.scala.ArtyomsTest.MyTestClass
import org.junit.experimental.categories.Category

@Category(Array(classOf[MyTests]))
class ArtyomsTest
  extends UsefulTestCase {

  def test1(): Unit = {
    val myTestClass = new MyTestClass
    myTestClass.myTestMethod()
  }

  def test2(): Unit = {
    val myTestClass = new MyTestClass
    myTestClass.myTestMethod()
    myTestClass.myTestMethod()
  }

  def test3(): Unit = {
    val cl = new Cl
    cl.myMethod()
    cl.myMethod()
    cl.myMethod()
  }
}

object ArtyomsTest {

  private class MyTestClass {
    def myTestMethod(): Unit = ()
  }
}
