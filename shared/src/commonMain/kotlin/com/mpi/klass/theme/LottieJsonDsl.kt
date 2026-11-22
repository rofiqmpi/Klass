package com.mpi.klass.theme

import kotlin.math.roundToInt

/**
 * Lightweight Bodymovin / Lottie 5.7.4 JSON builder for pure Kotlin Multiplatform icons.
 * Generates valid, multi-layer duotone + stroke + trim-path Lottie JSON strings
 * rendered smoothly at 60 FPS by Compottie.
 */
internal object LottieJsonDsl {

    data class BezierEase(val ox: Double, val oy: Double, val ix: Double, val iy: Double) {
        companion object {
            val Snappy = BezierEase(0.22, 0.0, 0.16, 1.0)
            val Overshoot = BezierEase(0.25, 0.0, 0.25, 1.32)
            val Smooth = BezierEase(0.4, 0.0, 0.2, 1.0)
            val Linear = BezierEase(0.0, 0.0, 1.0, 1.0)
        }
    }

    data class Keyframe1D(
        val t: Int,
        val v: Double,
        val ease: BezierEase = BezierEase.Snappy,
    )

    data class Keyframe2D(
        val t: Int,
        val x: Double,
        val y: Double,
        val ease: BezierEase = BezierEase.Snappy,
    )

    data class BezierPath(
        val closed: Boolean,
        val vertices: List<Pair<Double, Double>>,
        val inTangents: List<Pair<Double, Double>>? = null,
        val outTangents: List<Pair<Double, Double>>? = null,
    )

    private fun fmt(v: Double): String {
        val rounded = (v * 100.0).roundToInt() / 100.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toInt().toString()
        } else {
            rounded.toString()
        }
    }

    fun buildComposition(
        name: String,
        durationFrames: Int = 60,
        layers: List<String>,
    ): String = buildString {
        append("""{"v":"5.7.4","fr":60,"ip":0,"op":$durationFrames,"w":100,"h":100,"nm":"$name","ddd":0,"assets":[],"layers":[""")
        append(layers.joinToString(","))
        append("]}")
    }

    fun shapeLayer(
        index: Int,
        name: String,
        durationFrames: Int = 60,
        anchor: Pair<Double, Double> = 50.0 to 50.0,
        position: String = staticVec3(anchor.first, anchor.second),
        scale: String = staticVec3(100.0, 100.0, 100.0),
        rotation: String = staticScalar(0.0),
        opacity: String = staticScalar(100.0),
        shapes: List<String>,
    ): String {
        val (ax, ay) = anchor
        return """{"ddd":0,"ind":$index,"ty":4,"nm":"$name","sr":1,"ks":{"o":$opacity,"r":$rotation,"p":$position,"a":${staticVec3(ax, ay)},"s":$scale},"ao":0,"shapes":[${shapes.joinToString(",")}],"ip":0,"op":$durationFrames,"st":0,"bm":0}"""
    }

    fun group(
        name: String = "G",
        items: List<String>,
        pos: Pair<Double, Double> = 0.0 to 0.0,
        scale: String = staticVec2(100.0, 100.0),
        rotation: String = staticScalar(0.0),
        opacity: String = staticScalar(100.0),
    ): String {
        val tr = """{"ty":"tr","p":${staticVec2(pos.first, pos.second)},"a":${staticVec2(0.0, 0.0)},"s":$scale,"r":$rotation,"o":$opacity,"sk":${staticScalar(0.0)},"sa":${staticScalar(0.0)},"nm":"Transform"}"""
        return """{"ty":"gr","it":[${items.joinToString(",")},$tr],"nm":"$name","bm":0}"""
    }

    fun polyline(
        points: List<Pair<Double, Double>>,
        closed: Boolean = false,
    ): String = path(BezierPath(closed = closed, vertices = points))

    fun path(bp: BezierPath): String {
        val vStr = bp.vertices.joinToString(",") { "[${fmt(it.first)},${fmt(it.second)}]" }
        val iPts = bp.inTangents ?: List(bp.vertices.size) { 0.0 to 0.0 }
        val oPts = bp.outTangents ?: List(bp.vertices.size) { 0.0 to 0.0 }
        val iStr = iPts.joinToString(",") { "[${fmt(it.first)},${fmt(it.second)}]" }
        val oStr = oPts.joinToString(",") { "[${fmt(it.first)},${fmt(it.second)}]" }
        return """{"d":1,"ty":"sh","ks":{"a":0,"k":{"c":${bp.closed},"v":[$vStr],"i":[$iStr],"o":[$oStr]}},"nm":"Path"}"""
    }

    fun ellipse(cx: Double, cy: Double, w: Double, h: Double = w): String =
        """{"d":1,"ty":"el","p":${staticVec2(cx, cy)},"s":${staticVec2(w, h)},"nm":"Ellipse"}"""

    fun rect(cx: Double, cy: Double, w: Double, h: Double, r: Double = 0.0): String =
        """{"d":1,"ty":"rc","p":${staticVec2(cx, cy)},"s":${staticVec2(w, h)},"r":${staticScalar(r)},"nm":"Rect"}"""

    fun stroke(width: Double = 6.5, opacityPct: Double = 100.0, dashed: Boolean = false): String {
        val dash = if (dashed) ""","d":[{"n":"d","nm":"dash","v":${staticScalar(10.0)}},{"n":"g","nm":"gap","v":${staticScalar(8.0)}}]""" else ""
        return """{"ty":"st","c":{"a":0,"k":[1,1,1,1]},"o":${staticScalar(opacityPct)},"w":${staticScalar(width)},"lc":2,"lj":2,"ml":4$dash,"bm":0,"nm":"Stroke"}"""
    }

    fun duotoneFill(opacityPct: Double = 22.0): String =
        """{"ty":"fl","c":{"a":0,"k":[1,1,1,1]},"o":${staticScalar(opacityPct)},"r":1,"bm":0,"nm":"Fill"}"""

    fun solidFill(opacityPct: Double = 100.0): String = duotoneFill(opacityPct)

    fun trimPath(
        start: String = staticScalar(0.0),
        end: String = staticScalar(100.0),
        offset: String = staticScalar(0.0),
    ): String = """{"ty":"tm","s":$start,"e":$end,"o":$offset,"m":1,"nm":"Trim"}"""

    fun staticScalar(v: Double): String = """{"a":0,"k":${fmt(v)}}"""

    fun staticVec2(x: Double, y: Double): String = """{"a":0,"k":[${fmt(x)},${fmt(y)}]}"""

    fun staticVec3(x: Double, y: Double, z: Double = 0.0): String =
        """{"a":0,"k":[${fmt(x)},${fmt(y)},${fmt(z)}]}"""

    fun animScalar(keyframes: List<Keyframe1D>): String {
        val items = keyframes.mapIndexed { idx, kf ->
            if (idx == keyframes.lastIndex) {
                """{"t":${kf.t},"s":[${fmt(kf.v)}]}"""
            } else {
                val e = kf.ease
                """{"t":${kf.t},"s":[${fmt(kf.v)}],"i":{"x":[${fmt(e.ix)}],"y":[${fmt(e.iy)}]},"o":{"x":[${fmt(e.ox)}],"y":[${fmt(e.oy)}]}}"""
            }
        }
        return """{"a":1,"k":[${items.joinToString(",")}]}"""
    }

    fun animVec2(keyframes: List<Keyframe2D>, z: Double = 0.0): String {
        val items = keyframes.mapIndexed { idx, kf ->
            if (idx == keyframes.lastIndex) {
                """{"t":${kf.t},"s":[${fmt(kf.x)},${fmt(kf.y)},${fmt(z)}]}"""
            } else {
                val e = kf.ease
                """{"t":${kf.t},"s":[${fmt(kf.x)},${fmt(kf.y)},${fmt(z)}],"i":{"x":[${fmt(e.ix)},${fmt(e.ix)}],"y":[${fmt(e.iy)},${fmt(e.iy)}]},"o":{"x":[${fmt(e.ox)},${fmt(e.ox)}],"y":[${fmt(e.oy)},${fmt(e.oy)}]}}"""
            }
        }
        return """{"a":1,"k":[${items.joinToString(",")}]}"""
    }

    fun animScale(keyframes: List<Keyframe2D>): String = animVec2(keyframes, z = 100.0)
}
