package com.mlab.influx.core

import org.apache.spark.util.AccumulatorV2
/**
  * Created by ravi on 4/4/17.
  */

abstract class MutableNode[A, B, C] extends Node[A, B] {
  var initialState: C

  val state: Accumulator[C] = new Accumulator(initialState)
  def update(in: C) : Unit
  def update(inStream: DStream[C]) : Unit = inStream.foreach(update)
  def apply(in: A) : B
  def apply(inStream: DStream[A]) : DStream[B] = inStream.map(apply)
  def reset : Unit = state.reset()

}

object MutableNode {

  def apply[A, B, C](f: A=>B, g: C=>C, initialVal: C) : MutableNode[A,B,C] = new MutableNode[A,B,C] {
    initialState = initialVal
    override def apply(in: A): B = f(in)
    override def apply(inStream: DStream[A]) : DStream[B] = inStream.map(f)
    override def update(in: C) : Unit = state.add(g(in))
  }
}

private class Accumulator[C](initialState: C) extends AccumulatorV2[C, C] {
  private var state: C = initialState

  def reset(): Unit = { state = initialState}
  def add(in: C): Unit = {state = in }

  override def isZero: Boolean = false

  override def copy(): AccumulatorV2[C, C] = new Accumulator(state)

  override def merge(other: AccumulatorV2[C, C]): Unit = { state = other.value }

  override def value: C = state
}
