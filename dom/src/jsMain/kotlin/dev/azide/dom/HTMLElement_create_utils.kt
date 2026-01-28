package dev.azide.dom

import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement

fun Document.createDivElement(): HTMLDivElement = createElement("div") as HTMLDivElement

fun Document.createButtonElement(): HTMLButtonElement = createElement("button") as HTMLButtonElement

fun Document.createSpanElement(): HTMLSpanElement = createElement("span") as HTMLSpanElement

fun Document.createCanvasElement(): HTMLCanvasElement = createElement("canvas") as HTMLCanvasElement

fun Document.createInputElement(): HTMLInputElement = createElement("input") as HTMLInputElement
