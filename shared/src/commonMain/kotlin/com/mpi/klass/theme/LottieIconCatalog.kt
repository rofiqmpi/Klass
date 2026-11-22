package com.mpi.klass.theme

import com.mpi.klass.theme.LottieJsonDsl.BezierEase
import com.mpi.klass.theme.LottieJsonDsl.BezierPath
import com.mpi.klass.theme.LottieJsonDsl.Keyframe1D
import com.mpi.klass.theme.LottieJsonDsl.Keyframe2D
import com.mpi.klass.theme.LottieJsonDsl.animScalar
import com.mpi.klass.theme.LottieJsonDsl.animScale
import com.mpi.klass.theme.LottieJsonDsl.animVec2
import com.mpi.klass.theme.LottieJsonDsl.buildComposition
import com.mpi.klass.theme.LottieJsonDsl.duotoneFill
import com.mpi.klass.theme.LottieJsonDsl.ellipse
import com.mpi.klass.theme.LottieJsonDsl.group
import com.mpi.klass.theme.LottieJsonDsl.path
import com.mpi.klass.theme.LottieJsonDsl.polyline
import com.mpi.klass.theme.LottieJsonDsl.rect
import com.mpi.klass.theme.LottieJsonDsl.shapeLayer
import com.mpi.klass.theme.LottieJsonDsl.solidFill
import com.mpi.klass.theme.LottieJsonDsl.staticScalar
import com.mpi.klass.theme.LottieJsonDsl.stroke
import com.mpi.klass.theme.LottieJsonDsl.trimPath

/**
 * Curated Bodymovin Lottie JSON catalog for every interactive icon across Klass.
 * Every icon uses soft duotone backing fills + crisp rounded strokes + multi-layer
 * staggered cubic-bezier animations designed on a 100x100 artboard at 60 FPS.
 */
internal object LottieIconCatalog {

    private val cache = HashMap<String, String>()

    fun jsonFor(iconName: String): String = cache.getOrPut(iconName) {
        when (iconName) {
            "Notification" -> notificationJson()
            "MessageBubble" -> messageBubbleJson()
            "Refresh" -> refreshJson()
            "TrashCan" -> trashCanJson()
            "PhoneSolid" -> phoneJson()
            "Sparkle" -> sparkleJson()
            "UsersGroup" -> usersGroupJson()
            "Download" -> downloadJson()
            "Search" -> searchJson()
            "LightMode" -> sunJson()
            "DarkMode" -> moonJson()
            "AutoMode" -> autoThemeJson()
            "Filter" -> filterJson()
            "Plus" -> plusJson()
            "Send" -> sendJson()
            "Droplet" -> dropletJson()
            "Check" -> checkJson()
            "Book" -> bookJson()
            "CalendarDays" -> calendarJson()
            "GraduationCap" -> graduationCapJson()
            "DoorOpen" -> doorOpenJson()
            "Crown" -> crownJson()
            "Shield" -> shieldJson()
            "Palette" -> paletteJson()
            "Clock" -> clockJson()
            "Edit" -> editJson()
            "Person", "SolidUser", "UserTie", "UserNurse" -> personJson()
            "Lock" -> lockJson()
            "Login" -> portalArrowJson(isLogin = true)
            "Logout" -> portalArrowJson(isLogin = false)
            "InfoCircle" -> infoCircleJson()
            "HelpCircle" -> helpCircleJson()
            "Home" -> homeJson()
            "Award" -> awardJson()
            "Close" -> closeJson()
            "BackArrow" -> arrowJson(left = true)
            "RightArrow" -> arrowJson(left = false)
            "Camera" -> cameraJson()
            "LanguageGlobe" -> globeJson()
            "IdCard", "Registration" -> idCardJson()
            "FileLines" -> fileLinesJson()
            "Database" -> databaseJson()
            "LocationDot" -> locationDotJson()
            "EnvelopeSolid" -> envelopeJson()
            "Eye" -> eyeJson(hidden = false)
            "EyeOff" -> eyeJson(hidden = true)
            "Copy" -> copyJson()
            "Pin" -> pinJson()
            "HandWave" -> handWaveJson()
            "UserPlus" -> userPlusJson()
            "Swap" -> swapJson()
            "Warning" -> warningJson()
            "ChevronRight" -> chevronJson(down = false)
            "ChevronDown" -> chevronJson(down = true)
            "MoreVert" -> moreDotsJson(vertical = true)
            "MoreHoriz" -> moreDotsJson(vertical = false)
            "LayerGroup" -> layerGroupJson()
            "UsersRectangle" -> usersGroupJson()
            "VenusMars" -> venusMarsJson()
            "Ring" -> ringJson()
            "PrayingHands" -> heartHandsJson()
            else -> genericPulseJson()
        }
    }

    private fun notificationJson(): String {
        val bellPath = BezierPath(
            closed = true,
            vertices = listOf(
                50.0 to 19.0,
                73.0 to 42.0,
                81.0 to 69.0,
                19.0 to 69.0,
                27.0 to 42.0,
            ),
            inTangents = listOf(
                -13.0 to 0.0,
                0.0 to -13.0,
                -3.0 to -8.0,
                0.0 to 0.0,
                0.0 to 12.0,
            ),
            outTangents = listOf(
                13.0 to 0.0,
                0.0 to 12.0,
                0.0 to 0.0,
                3.0 to -8.0,
                0.0 to -13.0,
            ),
        )
        val clapperLayer = shapeLayer(
            index = 1,
            name = "Clapper",
            anchor = 50.0 to 19.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 19.0),
                    Keyframe2D(12, 55.0, 19.0),
                    Keyframe2D(24, 44.0, 19.0),
                    Keyframe2D(36, 54.0, 19.0),
                    Keyframe2D(48, 47.0, 19.0),
                    Keyframe2D(60, 50.0, 19.0),
                )
            ),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(12, 12.0),
                    Keyframe1D(24, -14.0),
                    Keyframe1D(36, 10.0),
                    Keyframe1D(48, -6.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(41.0 to 72.0, 50.0 to 82.0, 59.0 to 72.0)),
                        stroke(6.5),
                    )
                )
            ),
        )
        val bellLayer = shapeLayer(
            index = 2,
            name = "Bell",
            anchor = 50.0 to 19.0,
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(10, -18.0, BezierEase.Overshoot),
                    Keyframe1D(22, 16.0),
                    Keyframe1D(34, -11.0),
                    Keyframe1D(46, 6.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        path(bellPath),
                        duotoneFill(22.0),
                        stroke(6.5),
                    )
                ),
                group(
                    items = listOf(
                        ellipse(50.0, 14.0, 8.0, 8.0),
                        solidFill(),
                    )
                ),
            ),
        )
        val wavesLayer = shapeLayer(
            index = 3,
            name = "Waves",
            opacity = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(10, 95.0),
                    Keyframe1D(38, 70.0),
                    Keyframe1D(55, 0.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(80.0 to 27.0, 86.0 to 38.0, 84.0 to 49.0)),
                        polyline(listOf(20.0 to 27.0, 14.0 to 38.0, 16.0 to 49.0)),
                        trimPath(
                            end = animScalar(
                                listOf(
                                    Keyframe1D(0, 0.0),
                                    Keyframe1D(20, 100.0),
                                    Keyframe1D(55, 100.0),
                                    Keyframe1D(60, 0.0),
                                )
                            )
                        ),
                        stroke(5.5),
                    )
                )
            ),
        )
        return buildComposition("tg_notification", 60, listOf(wavesLayer, bellLayer, clapperLayer))
    }

    private fun messageBubbleJson(): String {
        val bubble = BezierPath(
            closed = true,
            vertices = listOf(
                50.0 to 16.0,
                84.0 to 48.0,
                50.0 to 80.0,
                34.0 to 77.0,
                16.0 to 85.0,
                23.0 to 67.0,
                16.0 to 48.0,
            ),
            inTangents = listOf(
                -20.0 to 0.0,
                0.0 to -18.0,
                20.0 to 0.0,
                6.0 to 2.0,
                0.0 to 0.0,
                -4.0 to 7.0,
                0.0 to 18.0,
            ),
            outTangents = listOf(
                20.0 to 0.0,
                0.0 to 18.0,
                -6.0 to 0.0,
                0.0 to 0.0,
                0.0 to 0.0,
                -5.0 to -6.0,
                0.0 to -18.0,
            ),
        )
        val bubbleLayer = shapeLayer(
            index = 1,
            name = "Bubble",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(15, 107.0, 107.0, BezierEase.Overshoot),
                    Keyframe2D(34, 97.0, 97.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(path(bubble), duotoneFill(20.0), stroke(6.5)))
            ),
        )

        fun dotLayer(idx: Int, cx: Double, startT: Int): String = shapeLayer(
            index = idx,
            name = "Dot$idx",
            anchor = cx to 49.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, cx, 49.0),
                    Keyframe2D(startT, cx, 49.0),
                    Keyframe2D(startT + 9, cx, 39.0, BezierEase.Overshoot),
                    Keyframe2D(startT + 20, cx, 51.0),
                    Keyframe2D(startT + 28, cx, 49.0),
                    Keyframe2D(60, cx, 49.0),
                )
            ),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(startT, 100.0, 100.0),
                    Keyframe2D(startT + 9, 130.0, 130.0),
                    Keyframe2D(startT + 22, 100.0, 100.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(cx, 49.0, 8.5, 8.5), solidFill()))
            ),
        )

        return buildComposition(
            "tg_message",
            60,
            listOf(dotLayer(4, 65.0, 22), dotLayer(3, 50.0, 14), dotLayer(2, 35.0, 6), bubbleLayer),
        )
    }

    private fun refreshJson(): String {
        val topArc = BezierPath(
            closed = false,
            vertices = listOf(22.0 to 44.0, 50.0 to 20.0, 77.0 to 39.0),
            inTangents = listOf(0.0 to 0.0, -16.0 to 0.0, -7.0 to -12.0),
            outTangents = listOf(5.0 to -14.0, 16.0 to 0.0, 0.0 to 0.0),
        )
        val botArc = BezierPath(
            closed = false,
            vertices = listOf(78.0 to 56.0, 50.0 to 80.0, 23.0 to 61.0),
            inTangents = listOf(0.0 to 0.0, 16.0 to 0.0, 7.0 to 12.0),
            outTangents = listOf(-5.0 to 14.0, -16.0 to 0.0, 0.0 to 0.0),
        )
        val spinner = shapeLayer(
            index = 1,
            name = "Arrows",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0, BezierEase.Smooth),
                    Keyframe1D(60, 360.0),
                )
            ),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(30, 108.0, 108.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 50.0, 56.0, 56.0), duotoneFill(14.0))),
                group(
                    items = listOf(
                        path(topArc),
                        polyline(listOf(64.0 to 39.0, 78.0 to 41.0, 78.0 to 26.0)),
                        path(botArc),
                        polyline(listOf(36.0 to 61.0, 22.0 to 59.0, 22.0 to 74.0)),
                        stroke(6.8),
                    )
                ),
            ),
        )
        return buildComposition("tg_refresh", 60, listOf(spinner))
    }

    private fun trashCanJson(): String {
        val binBody = shapeLayer(
            index = 1,
            name = "Bin",
            anchor = 50.0 to 84.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 84.0), Keyframe2D(60, 50.0, 84.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(36, 100.0, 100.0),
                    Keyframe2D(46, 106.0, 92.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(
                            listOf(28.0 to 37.0, 33.0 to 83.0, 67.0 to 83.0, 72.0 to 37.0),
                            closed = true,
                        ),
                        duotoneFill(22.0),
                        stroke(6.5),
                    )
                ),
                group(
                    items = listOf(
                        polyline(listOf(43.0 to 48.0, 44.0 to 72.0)),
                        polyline(listOf(57.0 to 48.0, 56.0 to 72.0)),
                        stroke(6.0),
                    )
                ),
            ),
        )
        val lid = shapeLayer(
            index = 2,
            name = "Lid",
            anchor = 24.0 to 30.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 24.0, 30.0),
                    Keyframe2D(14, 24.0, 22.0, BezierEase.Overshoot),
                    Keyframe2D(36, 24.0, 23.0),
                    Keyframe2D(48, 24.0, 30.0),
                    Keyframe2D(60, 24.0, 30.0),
                )
            ),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(16, -26.0, BezierEase.Overshoot),
                    Keyframe1D(34, -20.0),
                    Keyframe1D(48, 4.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(21.0 to 30.0, 79.0 to 30.0)),
                        polyline(listOf(39.0 to 30.0, 41.0 to 18.0, 59.0 to 18.0, 61.0 to 30.0)),
                        stroke(6.5),
                    )
                )
            ),
        )
        return buildComposition("tg_trash", 60, listOf(lid, binBody))
    }

    private fun phoneJson(): String {
        val handset = BezierPath(
            closed = true,
            vertices = listOf(
                24.0 to 21.0,
                38.0 to 21.0,
                44.0 to 36.0,
                35.0 to 45.0,
                55.0 to 65.0,
                64.0 to 56.0,
                79.0 to 62.0,
                79.0 to 76.0,
                67.0 to 82.0,
                18.0 to 33.0,
            ),
            inTangents = List(10) { 0.0 to 0.0 }.toMutableList().apply {
                this[8] = 10.0 to 0.0
                this[9] = 0.0 to 10.0
            },
            outTangents = List(10) { 0.0 to 0.0 }.toMutableList().apply {
                this[8] = -26.0 to -6.0
                this[9] = -2.0 to -9.0
            },
        )
        val bodyLayer = shapeLayer(
            index = 1,
            name = "Handset",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(8, -14.0),
                    Keyframe1D(18, 14.0),
                    Keyframe1D(28, -12.0),
                    Keyframe1D(38, 10.0),
                    Keyframe1D(48, -5.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(handset), duotoneFill(22.0), stroke(6.2))))
        )
        val arc1 = BezierPath(
            closed = false,
            vertices = listOf(56.0 to 24.0, 72.0 to 30.0, 76.0 to 44.0),
            inTangents = listOf(0.0 to 0.0, -5.0 to -4.0, -2.0 to -6.0),
            outTangents = listOf(6.0 to 0.0, 4.0 to 4.0, 0.0 to 0.0),
        )
        val arc2 = BezierPath(
            closed = false,
            vertices = listOf(58.0 to 13.0, 81.0 to 22.0, 87.0 to 44.0),
            inTangents = listOf(0.0 to 0.0, -8.0 to -6.0, -2.0 to -8.0),
            outTangents = listOf(10.0 to 0.0, 6.0 to 6.0, 0.0 to 0.0),
        )
        val ringLayer = shapeLayer(
            index = 2,
            name = "RingWaves",
            opacity = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(12, 100.0),
                    Keyframe1D(42, 85.0),
                    Keyframe1D(56, 0.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        path(arc1),
                        path(arc2),
                        trimPath(
                            end = animScalar(
                                listOf(
                                    Keyframe1D(0, 0.0),
                                    Keyframe1D(26, 100.0),
                                    Keyframe1D(60, 100.0),
                                )
                            )
                        ),
                        stroke(5.8),
                    )
                )
            ),
        )
        return buildComposition("tg_phone", 60, listOf(ringLayer, bodyLayer))
    }

    private fun sparkleJson(): String {
        val mainStar = BezierPath(
            closed = true,
            vertices = listOf(47.0 to 15.0, 79.0 to 50.0, 47.0 to 85.0, 15.0 to 50.0),
            inTangents = listOf(-4.0 to 24.0, -24.0 to -4.0, 4.0 to -24.0, 24.0 to 4.0),
            outTangents = listOf(4.0 to 24.0, -24.0 to 4.0, -4.0 to -24.0, 24.0 to -4.0),
        )
        val bigStarLayer = shapeLayer(
            index = 1,
            name = "MainSparkle",
            anchor = 47.0 to 50.0,
            position = animVec2(listOf(Keyframe2D(0, 47.0, 50.0), Keyframe2D(60, 47.0, 50.0))),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0, BezierEase.Overshoot),
                    Keyframe1D(44, 90.0),
                    Keyframe1D(60, 90.0),
                )
            ),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(22, 114.0, 114.0, BezierEase.Overshoot),
                    Keyframe2D(44, 94.0, 94.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(mainStar), duotoneFill(24.0), stroke(6.2))))
        )
        val miniStar = BezierPath(
            closed = true,
            vertices = listOf(78.0 to 14.0, 88.0 to 24.0, 78.0 to 34.0, 68.0 to 24.0),
            inTangents = listOf(-2.0 to 7.0, -7.0 to -2.0, 2.0 to -7.0, 7.0 to 2.0),
            outTangents = listOf(2.0 to 7.0, -7.0 to 2.0, -2.0 to -7.0, 7.0 to -2.0),
        )
        val twinkleTop = shapeLayer(
            index = 2,
            name = "TwinkleTop",
            anchor = 78.0 to 24.0,
            position = animVec2(listOf(Keyframe2D(0, 78.0, 24.0), Keyframe2D(60, 78.0, 24.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(18, 30.0, 30.0),
                    Keyframe2D(38, 130.0, 130.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(miniStar), solidFill())))
        )
        val twinkleDot = shapeLayer(
            index = 3,
            name = "TwinkleBot",
            anchor = 22.0 to 79.0,
            position = animVec2(listOf(Keyframe2D(0, 22.0, 79.0), Keyframe2D(60, 22.0, 79.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(26, 135.0, 135.0),
                    Keyframe2D(46, 40.0, 40.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(22.0, 79.0, 8.5, 8.5), solidFill())))
        )
        return buildComposition("tg_sparkle", 60, listOf(twinkleDot, twinkleTop, bigStarLayer))
    }

    private fun usersGroupJson(): String {
        val shoulders1 = BezierPath(
            closed = true,
            vertices = listOf(14.0 to 80.0, 38.0 to 58.0, 62.0 to 80.0),
            inTangents = listOf(0.0 to 0.0, -14.0 to 0.0, 0.0 to 0.0),
            outTangents = listOf(0.0 to -14.0, 14.0 to 0.0, 0.0 to 0.0),
        )
        val primary = shapeLayer(
            index = 1,
            name = "User1",
            anchor = 38.0 to 56.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 38.0, 56.0),
                    Keyframe2D(16, 38.0, 49.0, BezierEase.Overshoot),
                    Keyframe2D(34, 38.0, 56.0),
                    Keyframe2D(60, 38.0, 56.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(38.0, 33.0, 24.0, 24.0), duotoneFill(24.0), stroke(6.2))),
                group(items = listOf(path(shoulders1), duotoneFill(22.0), stroke(6.2))),
            ),
        )
        val shoulder2 = BezierPath(
            closed = false,
            vertices = listOf(69.0 to 60.0, 86.0 to 80.0),
            inTangents = listOf(0.0 to 0.0, 0.0 to -12.0),
            outTangents = listOf(12.0 to 2.0, 0.0 to 0.0),
        )
        val secondary = shapeLayer(
            index = 2,
            name = "User2",
            anchor = 72.0 to 56.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 72.0, 56.0),
                    Keyframe2D(14, 72.0, 56.0),
                    Keyframe2D(30, 72.0, 50.0, BezierEase.Overshoot),
                    Keyframe2D(48, 72.0, 56.0),
                    Keyframe2D(60, 72.0, 56.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(68.0, 35.0, 20.0, 20.0), duotoneFill(18.0), stroke(6.0))),
                group(items = listOf(path(shoulder2), stroke(6.0))),
            ),
        )
        return buildComposition("tg_users", 60, listOf(secondary, primary))
    }

    private fun downloadJson(): String {
        val tray = shapeLayer(
            index = 1,
            name = "Tray",
            anchor = 50.0 to 78.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 78.0),
                    Keyframe2D(22, 50.0, 78.0),
                    Keyframe2D(32, 50.0, 82.0),
                    Keyframe2D(46, 50.0, 78.0),
                    Keyframe2D(60, 50.0, 78.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(20.0 to 65.0, 20.0 to 81.0, 80.0 to 81.0, 80.0 to 65.0)),
                        stroke(6.8),
                    )
                )
            ),
        )
        val arrow = shapeLayer(
            index = 2,
            name = "Arrow",
            anchor = 50.0 to 40.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 40.0),
                    Keyframe2D(14, 50.0, 31.0),
                    Keyframe2D(30, 50.0, 49.0, BezierEase.Overshoot),
                    Keyframe2D(48, 50.0, 40.0),
                    Keyframe2D(60, 50.0, 40.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 40.0, 46.0, 46.0), duotoneFill(16.0))),
                group(
                    items = listOf(
                        polyline(listOf(50.0 to 16.0, 50.0 to 61.0)),
                        polyline(listOf(33.0 to 44.0, 50.0 to 62.0, 67.0 to 44.0)),
                        stroke(6.8),
                    )
                ),
            ),
        )
        return buildComposition("tg_download", 60, listOf(tray, arrow))
    }

    private fun searchJson(): String {
        val glint = BezierPath(
            closed = false,
            vertices = listOf(31.0 to 36.0, 43.0 to 27.0),
            inTangents = listOf(0.0 to 0.0, -6.0 to 0.0),
            outTangents = listOf(3.0 to -6.0, 0.0 to 0.0),
        )
        val glass = shapeLayer(
            index = 1,
            name = "Magnifier",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(18, -14.0, BezierEase.Overshoot),
                    Keyframe1D(38, 8.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(43.0, 43.0, 48.0, 48.0), duotoneFill(20.0), stroke(6.5))),
                group(items = listOf(polyline(listOf(61.0 to 61.0, 83.0 to 83.0)), stroke(7.2))),
                group(
                    items = listOf(
                        path(glint),
                        trimPath(
                            end = animScalar(
                                listOf(
                                    Keyframe1D(0, 0.0),
                                    Keyframe1D(28, 100.0),
                                    Keyframe1D(52, 0.0),
                                    Keyframe1D(60, 0.0),
                                )
                            )
                        ),
                        stroke(4.8),
                    )
                ),
            ),
        )
        return buildComposition("tg_search", 60, listOf(glass))
    }

    private fun sunJson(): String {
        val core = shapeLayer(
            index = 1,
            name = "SunCore",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(28, 114.0, 114.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 50.0, 36.0, 36.0), duotoneFill(26.0), stroke(6.4)))
            ),
        )
        val rays = listOf(
            (50.0 to 14.0) to (50.0 to 23.0),
            (50.0 to 77.0) to (50.0 to 86.0),
            (14.0 to 50.0) to (23.0 to 50.0),
            (77.0 to 50.0) to (86.0 to 50.0),
            (24.5 to 24.5) to (31.0 to 31.0),
            (69.0 to 69.0) to (75.5 to 75.5),
            (75.5 to 24.5) to (69.0 to 31.0),
            (31.0 to 69.0) to (24.5 to 75.5),
        )
        val raysLayer = shapeLayer(
            index = 2,
            name = "Rays",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Smooth), Keyframe1D(60, 45.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(30, 110.0, 110.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = rays.map { polyline(listOf(it.first, it.second)) } + stroke(6.2))
            ),
        )
        return buildComposition("tg_sun", 60, listOf(raysLayer, core))
    }

    private fun moonJson(): String {
        val crescent = BezierPath(
            closed = true,
            vertices = listOf(48.0 to 16.0, 44.0 to 52.0, 82.0 to 60.0, 47.0 to 84.0, 17.0 to 50.0),
            inTangents = listOf(-14.0 to 2.0, 0.0 to -16.0, -10.0 to 6.0, 18.0 to 0.0, 0.0 to 18.0),
            outTangents = listOf(-8.0 to 12.0, 0.0 to 16.0, -4.0 to 14.0, -18.0 to 0.0, 0.0 to -18.0),
        )
        val moonLayer = shapeLayer(
            index = 1,
            name = "Moon",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(22, -16.0, BezierEase.Overshoot),
                    Keyframe1D(44, 6.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(crescent), duotoneFill(24.0), stroke(6.4))))
        )
        val star = shapeLayer(
            index = 2,
            name = "Star",
            anchor = 74.0 to 27.0,
            position = animVec2(listOf(Keyframe2D(0, 74.0, 27.0), Keyframe2D(60, 74.0, 27.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(24, 35.0, 35.0),
                    Keyframe2D(44, 130.0, 130.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(74.0, 27.0, 10.0, 10.0), solidFill())))
        )
        return buildComposition("tg_moon", 60, listOf(star, moonLayer))
    }

    private fun autoThemeJson(): String {
        val half = BezierPath(
            closed = true,
            vertices = listOf(50.0 to 20.0, 80.0 to 50.0, 50.0 to 80.0),
            inTangents = listOf(0.0 to 0.0, 0.0 to -16.5, 16.5 to 0.0),
            outTangents = listOf(16.5 to 0.0, 0.0 to 16.5, 0.0 to 0.0),
        )
        val layer = shapeLayer(
            index = 1,
            name = "AutoOrb",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Smooth), Keyframe1D(60, 180.0))),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 50.0, 62.0, 62.0), duotoneFill(16.0), stroke(6.5))),
                group(items = listOf(path(half), solidFill())),
            ),
        )
        return buildComposition("tg_auto", 60, listOf(layer))
    }

    private fun filterJson(): String {
        val tracks = shapeLayer(
            index = 1,
            name = "Tracks",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(16.0 to 27.0, 84.0 to 27.0)),
                        polyline(listOf(16.0 to 50.0, 84.0 to 50.0)),
                        polyline(listOf(16.0 to 73.0, 84.0 to 73.0)),
                        stroke(6.2),
                    )
                )
            ),
        )
        fun knob(idx: Int, y: Double, x1: Double, x2: Double): String = shapeLayer(
            index = idx,
            name = "Knob$idx",
            anchor = x1 to y,
            position = animVec2(
                listOf(
                    Keyframe2D(0, x1, y, BezierEase.Smooth),
                    Keyframe2D(30, x2, y, BezierEase.Smooth),
                    Keyframe2D(60, x1, y),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(x1, y, 16.0, 16.0), solidFill())))
        )
        return buildComposition(
            "tg_filter",
            60,
            listOf(tracks, knob(2, 27.0, 36.0, 68.0), knob(3, 50.0, 66.0, 34.0), knob(4, 73.0, 42.0, 66.0)),
        )
    }

    private fun plusJson(): String {
        val halo = shapeLayer(
            index = 1,
            name = "Halo",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(26, 110.0, 110.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 68.0, 68.0), duotoneFill(18.0))))
        )
        val cross = shapeLayer(
            index = 2,
            name = "Cross",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Overshoot), Keyframe1D(44, 90.0), Keyframe1D(60, 90.0))),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(50.0 to 23.0, 50.0 to 77.0)),
                        polyline(listOf(23.0 to 50.0, 77.0 to 50.0)),
                        stroke(7.5),
                    )
                )
            ),
        )
        return buildComposition("tg_plus", 60, listOf(halo, cross))
    }

    private fun sendJson(): String {
        val plane = shapeLayer(
            index = 1,
            name = "PaperPlane",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(22, 60.0, 40.0, BezierEase.Overshoot),
                    Keyframe2D(44, 46.0, 54.0),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(20, -10.0),
                    Keyframe1D(44, 5.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(84.0 to 16.0, 58.0 to 84.0, 44.0 to 56.0, 16.0 to 42.0), closed = true),
                        duotoneFill(22.0),
                        stroke(6.4),
                    )
                ),
                group(items = listOf(polyline(listOf(82.0 to 18.0, 44.0 to 56.0)), stroke(6.0))),
            ),
        )
        return buildComposition("tg_send", 60, listOf(plane))
    }

    private fun dropletJson(): String {
        val drop = BezierPath(
            closed = true,
            vertices = listOf(50.0 to 14.0, 76.0 to 54.0, 50.0 to 82.0, 24.0 to 54.0),
            inTangents = listOf(0.0 to 0.0, 0.0 to -14.0, 15.0 to 0.0, 0.0 to 14.0),
            outTangents = listOf(0.0 to 0.0, 0.0 to 16.0, -15.0 to 0.0, 0.0 to -14.0),
        )
        val dropLayer = shapeLayer(
            index = 1,
            name = "Drop",
            anchor = 50.0 to 82.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 82.0), Keyframe2D(60, 50.0, 82.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(18, 112.0, 88.0),
                    Keyframe2D(36, 94.0, 108.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(drop), duotoneFill(26.0), stroke(6.5))))
        )
        val ripple = shapeLayer(
            index = 2,
            name = "Ripple",
            anchor = 50.0 to 85.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 85.0), Keyframe2D(60, 50.0, 85.0))),
            scale = animScale(listOf(Keyframe2D(0, 40.0, 40.0), Keyframe2D(48, 115.0, 115.0), Keyframe2D(60, 40.0, 40.0))),
            opacity = animScalar(listOf(Keyframe1D(0, 75.0), Keyframe1D(48, 0.0), Keyframe1D(60, 0.0))),
            shapes = listOf(group(items = listOf(ellipse(50.0, 85.0, 48.0, 12.0), stroke(4.5))))
        )
        return buildComposition("tg_droplet", 60, listOf(ripple, dropLayer))
    }

    private fun checkJson(): String {
        val circle = shapeLayer(
            index = 1,
            name = "Badge",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(24, 110.0, 110.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 70.0, 70.0), duotoneFill(20.0))))
        )
        val tick = shapeLayer(
            index = 2,
            name = "Tick",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(23.0 to 52.0, 42.0 to 71.0, 77.0 to 31.0)),
                        trimPath(
                            end = animScalar(
                                listOf(
                                    Keyframe1D(0, 100.0),
                                    Keyframe1D(4, 10.0),
                                    Keyframe1D(34, 100.0, BezierEase.Overshoot),
                                    Keyframe1D(60, 100.0),
                                )
                            )
                        ),
                        stroke(7.5),
                    )
                )
            ),
        )
        return buildComposition("tg_check", 60, listOf(circle, tick))
    }

    private fun bookJson(): String {
        val base = shapeLayer(
            index = 1,
            name = "BookCover",
            shapes = listOf(
                group(
                    items = listOf(
                        rect(34.0, 50.0, 32.0, 58.0, 6.0),
                        rect(66.0, 50.0, 32.0, 58.0, 6.0),
                        duotoneFill(20.0),
                        stroke(6.2),
                    )
                ),
                group(
                    items = listOf(
                        polyline(listOf(25.0 to 38.0, 41.0 to 38.0)),
                        polyline(listOf(25.0 to 52.0, 41.0 to 52.0)),
                        stroke(5.2, opacityPct = 65.0),
                    )
                ),
            ),
        )
        val page = shapeLayer(
            index = 2,
            name = "FlipPage",
            anchor = 50.0 to 50.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(60, 50.0, 50.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(22, -100.0, 106.0, BezierEase.Smooth),
                    Keyframe2D(46, 100.0, 100.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        rect(66.0, 50.0, 30.0, 56.0, 5.0),
                        duotoneFill(26.0),
                        stroke(5.6),
                    )
                ),
                group(
                    items = listOf(
                        polyline(listOf(58.0 to 38.0, 74.0 to 38.0)),
                        polyline(listOf(58.0 to 52.0, 74.0 to 52.0)),
                        stroke(5.5),
                    )
                ),
            ),
        )
        return buildComposition("tg_book", 60, listOf(base, page))
    }

    private fun calendarJson(): String {
        val frame = shapeLayer(
            index = 1,
            name = "Calendar",
            shapes = listOf(
                group(items = listOf(rect(50.0, 54.0, 66.0, 60.0, 10.0), duotoneFill(20.0), stroke(6.2))),
                group(
                    items = listOf(
                        polyline(listOf(17.0 to 40.0, 83.0 to 40.0)),
                        polyline(listOf(34.0 to 16.0, 34.0 to 30.0)),
                        polyline(listOf(66.0 to 16.0, 66.0 to 30.0)),
                        stroke(6.2),
                    )
                ),
            ),
        )
        val dot = shapeLayer(
            index = 2,
            name = "DateDot",
            anchor = 50.0 to 60.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 60.0), Keyframe2D(60, 50.0, 60.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(20, 135.0, 135.0, BezierEase.Overshoot),
                    Keyframe2D(44, 90.0, 90.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(rect(36.0, 56.0, 10.0, 10.0, 3.0), rect(50.0, 56.0, 10.0, 10.0, 3.0), rect(64.0, 56.0, 10.0, 10.0, 3.0), solidFill()))
            ),
        )
        return buildComposition("tg_calendar", 60, listOf(frame, dot))
    }

    private fun graduationCapJson(): String {
        val cap = shapeLayer(
            index = 1,
            name = "Cap",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(18, -10.0, BezierEase.Overshoot),
                    Keyframe1D(40, 7.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(50.0 to 20.0, 88.0 to 39.0, 50.0 to 58.0, 12.0 to 39.0), closed = true),
                        duotoneFill(24.0),
                        stroke(6.2),
                    )
                ),
                group(
                    items = listOf(
                        polyline(listOf(28.0 to 48.0, 28.0 to 70.0, 50.0 to 79.0, 72.0 to 70.0, 72.0 to 48.0)),
                        stroke(6.0),
                    )
                ),
            ),
        )
        val tassel = shapeLayer(
            index = 2,
            name = "Tassel",
            anchor = 83.0 to 41.0,
            position = animVec2(listOf(Keyframe2D(0, 83.0, 41.0), Keyframe2D(60, 83.0, 41.0))),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(16, 20.0),
                    Keyframe1D(34, -15.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(group(items = listOf(polyline(listOf(83.0 to 41.0, 83.0 to 67.0)), stroke(6.0))))
        )
        return buildComposition("tg_graduation", 60, listOf(tassel, cap))
    }

    private fun doorOpenJson(): String {
        val frame = shapeLayer(
            index = 1,
            name = "Frame",
            shapes = listOf(
                group(items = listOf(rect(50.0, 50.0, 52.0, 68.0, 6.0), duotoneFill(14.0), stroke(6.2)))
            ),
        )
        val leaf = shapeLayer(
            index = 2,
            name = "DoorLeaf",
            anchor = 24.0 to 50.0,
            position = animVec2(listOf(Keyframe2D(0, 24.0, 50.0), Keyframe2D(60, 24.0, 50.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(26, 60.0, 100.0, BezierEase.Smooth),
                    Keyframe2D(54, 100.0, 100.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(rect(50.0, 50.0, 50.0, 66.0, 4.0), duotoneFill(22.0), stroke(6.0))),
                group(items = listOf(ellipse(63.0, 50.0, 7.0, 7.0), solidFill())),
            ),
        )
        return buildComposition("tg_door", 60, listOf(frame, leaf))
    }

    private fun crownJson(): String {
        val crown = shapeLayer(
            index = 1,
            name = "Crown",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(20, 50.0, 44.0, BezierEase.Overshoot),
                    Keyframe2D(46, 50.0, 50.0),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(
                            listOf(18.0 to 72.0, 18.0 to 34.0, 36.0 to 52.0, 50.0 to 24.0, 64.0 to 52.0, 82.0 to 34.0, 82.0 to 72.0),
                            closed = true,
                        ),
                        duotoneFill(24.0),
                        stroke(6.4),
                    )
                )
            ),
        )
        val jewels = shapeLayer(
            index = 2,
            name = "Jewels",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(24, 125.0, 125.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 18.0, 8.0, 8.0), ellipse(18.0, 27.0, 7.0, 7.0), ellipse(82.0, 27.0, 7.0, 7.0), solidFill()))
            ),
        )
        return buildComposition("tg_crown", 60, listOf(jewels, crown))
    }

    private fun shieldJson(): String {
        val shield = BezierPath(
            closed = true,
            vertices = listOf(50.0 to 14.0, 80.0 to 26.0, 76.0 to 57.0, 50.0 to 86.0, 24.0 to 57.0, 20.0 to 26.0),
            inTangents = listOf(0.0 to 0.0, 0.0 to 0.0, 3.0 to -12.0, 14.0 to -6.0, -3.0 to 14.0, 0.0 to 0.0),
            outTangents = listOf(0.0 to 0.0, 0.0 to 12.0, -4.0 to 14.0, -14.0 to -6.0, -3.0 to -12.0, 0.0 to 0.0),
        )
        val body = shapeLayer(
            index = 1,
            name = "Shield",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(24, 106.0, 106.0), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(path(shield), duotoneFill(22.0), stroke(6.4))))
        )
        val check = shapeLayer(
            index = 2,
            name = "Check",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(36.0 to 50.0, 46.0 to 60.0, 65.0 to 40.0)),
                        trimPath(end = animScalar(listOf(Keyframe1D(0, 100.0), Keyframe1D(6, 0.0), Keyframe1D(38, 100.0), Keyframe1D(60, 100.0)))),
                        stroke(6.4),
                    )
                )
            ),
        )
        return buildComposition("tg_shield", 60, listOf(body, check))
    }

    private fun paletteJson(): String {
        val palette = shapeLayer(
            index = 1,
            name = "Palette",
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(20, -14.0, BezierEase.Overshoot),
                    Keyframe1D(44, 8.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 50.0, 68.0, 62.0), duotoneFill(20.0), stroke(6.4))),
                group(items = listOf(ellipse(36.0, 36.0, 9.5, 9.5), ellipse(58.0, 34.0, 9.5, 9.5), ellipse(65.0, 54.0, 9.5, 9.5), solidFill())),
                group(items = listOf(ellipse(36.0, 62.0, 13.0, 13.0), stroke(5.0))),
            ),
        )
        return buildComposition("tg_palette", 60, listOf(palette))
    }

    private fun clockJson(): String {
        val face = shapeLayer(
            index = 1,
            name = "Dial",
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 68.0, 68.0), duotoneFill(20.0), stroke(6.4))))
        )
        val minuteHand = shapeLayer(
            index = 2,
            name = "Minute",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Smooth), Keyframe1D(60, 360.0))),
            shapes = listOf(group(items = listOf(polyline(listOf(50.0 to 50.0, 50.0 to 27.0)), stroke(6.2))))
        )
        val hourHand = shapeLayer(
            index = 3,
            name = "Hour",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Smooth), Keyframe1D(60, 30.0))),
            shapes = listOf(group(items = listOf(polyline(listOf(50.0 to 50.0, 66.0 to 50.0)), stroke(6.5))))
        )
        return buildComposition("tg_clock", 60, listOf(face, hourHand, minuteHand))
    }

    private fun editJson(): String {
        val pen = shapeLayer(
            index = 1,
            name = "Pencil",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(20, 57.0, 44.0),
                    Keyframe2D(40, 46.0, 52.0),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            rotation = animScalar(listOf(Keyframe1D(0, 0.0), Keyframe1D(20, -10.0), Keyframe1D(40, 8.0), Keyframe1D(60, 0.0))),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(22.0 to 78.0, 27.0 to 59.0, 67.0 to 19.0, 81.0 to 33.0, 41.0 to 73.0), closed = true),
                        duotoneFill(22.0),
                        stroke(6.2),
                    )
                )
            ),
        )
        val line = shapeLayer(
            index = 2,
            name = "Line",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(46.0 to 82.0, 82.0 to 82.0)),
                        trimPath(end = animScalar(listOf(Keyframe1D(0, 100.0), Keyframe1D(8, 10.0), Keyframe1D(44, 100.0), Keyframe1D(60, 100.0)))),
                        stroke(6.2),
                    )
                )
            ),
        )
        return buildComposition("tg_edit", 60, listOf(line, pen))
    }

    private fun personJson(): String {
        val head = shapeLayer(
            index = 1,
            name = "Head",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(20, 50.0, 45.0, BezierEase.Overshoot),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(50.0, 32.0, 26.0, 26.0), duotoneFill(24.0), stroke(6.4))))
        )
        val body = shapeLayer(
            index = 2,
            name = "Body",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(22.0 to 83.0, 28.0 to 63.0, 72.0 to 63.0, 78.0 to 83.0), closed = true),
                        duotoneFill(22.0),
                        stroke(6.4),
                    )
                )
            ),
        )
        return buildComposition("tg_person", 60, listOf(body, head))
    }

    private fun lockJson(): String {
        val shackle = shapeLayer(
            index = 1,
            name = "Shackle",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(18, 50.0, 41.0, BezierEase.Overshoot),
                    Keyframe2D(44, 50.0, 50.0),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(rect(50.0, 38.0, 34.0, 36.0, 17.0), stroke(6.5)))
            ),
        )
        val body = shapeLayer(
            index = 2,
            name = "Body",
            shapes = listOf(
                group(items = listOf(rect(50.0, 64.0, 54.0, 40.0, 10.0), duotoneFill(26.0), stroke(6.5))),
                group(items = listOf(ellipse(50.0, 64.0, 10.0, 10.0), solidFill())),
            ),
        )
        return buildComposition("tg_lock", 60, listOf(shackle, body))
    }

    private fun portalArrowJson(isLogin: Boolean): String {
        val frame = shapeLayer(
            index = 1,
            name = "Portal",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(54.0 to 20.0, 78.0 to 20.0, 78.0 to 80.0, 54.0 to 80.0)),
                        stroke(6.5),
                    )
                )
            ),
        )
        val shiftX = if (isLogin) 8.0 else -8.0
        val arrow = shapeLayer(
            index = 2,
            name = "Arrow",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(24, 50.0 + shiftX, 50.0, BezierEase.Overshoot),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(18.0 to 50.0, 58.0 to 50.0)),
                        polyline(
                            if (isLogin) listOf(42.0 to 34.0, 58.0 to 50.0, 42.0 to 66.0)
                            else listOf(34.0 to 34.0, 18.0 to 50.0, 34.0 to 66.0)
                        ),
                        stroke(6.5),
                    )
                )
            ),
        )
        return buildComposition(if (isLogin) "tg_login" else "tg_logout", 60, listOf(frame, arrow))
    }

    private fun infoCircleJson(): String {
        val ring = shapeLayer(
            index = 1,
            name = "Ring",
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 68.0, 68.0), duotoneFill(20.0), stroke(6.4))))
        )
        val glyph = shapeLayer(
            index = 2,
            name = "Glyph",
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 50.0),
                    Keyframe2D(20, 50.0, 44.0, BezierEase.Overshoot),
                    Keyframe2D(60, 50.0, 50.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 32.0, 8.0, 8.0), solidFill())),
                group(items = listOf(polyline(listOf(50.0 to 46.0, 50.0 to 68.0)), stroke(6.8))),
            ),
        )
        return buildComposition("tg_info", 60, listOf(ring, glyph))
    }

    private fun helpCircleJson(): String {
        val ring = shapeLayer(
            index = 1,
            name = "Ring",
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 68.0, 68.0), duotoneFill(20.0), stroke(6.4))))
        )
        val q = shapeLayer(
            index = 2,
            name = "Question",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0), Keyframe1D(18, -12.0), Keyframe1D(40, 10.0), Keyframe1D(60, 0.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(39.0 to 38.0, 46.0 to 30.0, 57.0 to 31.0, 61.0 to 40.0, 50.0 to 50.0, 50.0 to 56.0)), stroke(6.4))),
                group(items = listOf(ellipse(50.0, 69.0, 8.0, 8.0), solidFill())),
            ),
        )
        return buildComposition("tg_help", 60, listOf(ring, q))
    }

    private fun homeJson(): String {
        val house = shapeLayer(
            index = 1,
            name = "House",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(22, 108.0, 108.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(18.0 to 46.0, 50.0 to 18.0, 82.0 to 46.0, 82.0 to 82.0, 18.0 to 82.0), closed = true), duotoneFill(22.0), stroke(6.5))),
                group(items = listOf(rect(50.0, 68.0, 20.0, 28.0, 4.0), stroke(6.0))),
            ),
        )
        return buildComposition("tg_home", 60, listOf(house))
    }

    private fun awardJson(): String {
        val badge = shapeLayer(
            index = 1,
            name = "Medal",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0), Keyframe1D(18, -12.0), Keyframe1D(38, 10.0), Keyframe1D(60, 0.0))),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 40.0, 46.0, 46.0), duotoneFill(22.0), stroke(6.4))),
                group(items = listOf(polyline(listOf(38.0 to 60.0, 34.0 to 84.0, 50.0 to 76.0, 66.0 to 84.0, 62.0 to 60.0)), stroke(6.0))),
            ),
        )
        return buildComposition("tg_award", 60, listOf(badge))
    }

    private fun closeJson(): String {
        val cross = shapeLayer(
            index = 1,
            name = "Close",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0, BezierEase.Overshoot), Keyframe1D(44, 90.0), Keyframe1D(60, 90.0))),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(28.0 to 28.0, 72.0 to 72.0)),
                        polyline(listOf(72.0 to 28.0, 28.0 to 72.0)),
                        stroke(7.2),
                    )
                )
            ),
        )
        return buildComposition("tg_close", 60, listOf(cross))
    }

    private fun arrowJson(left: Boolean): String {
        val dx = if (left) -8.0 else 8.0
        val arrow = shapeLayer(
            index = 1,
            name = "Arrow",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(20, 50.0 + dx, 50.0, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(22.0 to 50.0, 78.0 to 50.0)),
                        polyline(
                            if (left) listOf(42.0 to 30.0, 22.0 to 50.0, 42.0 to 70.0)
                            else listOf(58.0 to 30.0, 78.0 to 50.0, 58.0 to 70.0)
                        ),
                        stroke(7.0),
                    )
                )
            ),
        )
        return buildComposition("tg_arrow", 60, listOf(arrow))
    }

    private fun cameraJson(): String {
        val body = shapeLayer(
            index = 1,
            name = "CameraBody",
            shapes = listOf(
                group(items = listOf(rect(50.0, 56.0, 72.0, 50.0, 11.0), duotoneFill(20.0), stroke(6.2))),
                group(items = listOf(polyline(listOf(34.0 to 31.0, 41.0 to 21.0, 59.0 to 21.0, 66.0 to 31.0), closed = true), duotoneFill(24.0), stroke(6.0))),
            ),
        )
        val lens = shapeLayer(
            index = 2,
            name = "Lens",
            anchor = 50.0 to 56.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 56.0), Keyframe2D(60, 50.0, 56.0))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(16, 62.0, 62.0),
                    Keyframe2D(34, 118.0, 118.0, BezierEase.Overshoot),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 56.0, 26.0, 26.0), duotoneFill(26.0), stroke(6.0))),
                group(items = listOf(ellipse(50.0, 56.0, 9.0, 9.0), solidFill())),
            ),
        )
        val flash = shapeLayer(
            index = 3,
            name = "Flash",
            anchor = 73.0 to 41.0,
            position = animVec2(listOf(Keyframe2D(0, 73.0, 41.0), Keyframe2D(60, 73.0, 41.0))),
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(18, 165.0, 165.0), Keyframe2D(42, 100.0, 100.0), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(ellipse(73.0, 41.0, 7.5, 7.5), solidFill()))),
        )
        return buildComposition("tg_camera", 60, listOf(body, lens, flash))
    }

    private fun globeJson(): String {
        val globe = shapeLayer(
            index = 1,
            name = "Globe",
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 50.0, 68.0, 68.0), duotoneFill(18.0), stroke(6.2))),
                group(items = listOf(polyline(listOf(18.0 to 50.0, 82.0 to 50.0)), polyline(listOf(50.0 to 16.0, 50.0 to 84.0)), stroke(5.2))),
            ),
        )
        val meridian = shapeLayer(
            index = 2,
            name = "Meridian",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(30, 35.0, 100.0, BezierEase.Smooth),
                    Keyframe2D(60, 100.0, 100.0, BezierEase.Smooth),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 34.0, 68.0), stroke(5.5)))),
        )
        return buildComposition("tg_globe", 60, listOf(globe, meridian))
    }

    private fun idCardJson(): String {
        val card = shapeLayer(
            index = 1,
            name = "Card",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(20, 105.0, 105.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(rect(50.0, 52.0, 74.0, 54.0, 10.0), duotoneFill(20.0), stroke(6.2))),
                group(items = listOf(ellipse(33.0, 48.0, 16.0, 16.0), solidFill())),
            ),
        )
        val lines = shapeLayer(
            index = 2,
            name = "Lines",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(51.0 to 43.0, 75.0 to 43.0)),
                        polyline(listOf(51.0 to 57.0, 71.0 to 57.0)),
                        trimPath(end = animScalar(listOf(Keyframe1D(0, 100.0), Keyframe1D(6, 0.0), Keyframe1D(38, 100.0), Keyframe1D(60, 100.0)))),
                        stroke(5.8),
                    )
                )
            ),
        )
        return buildComposition("tg_idcard", 60, listOf(card, lines))
    }

    private fun fileLinesJson(): String {
        val sheet = shapeLayer(
            index = 1,
            name = "Sheet",
            shapes = listOf(
                group(items = listOf(polyline(listOf(24.0 to 16.0, 60.0 to 16.0, 76.0 to 32.0, 76.0 to 84.0, 24.0 to 84.0), closed = true), duotoneFill(20.0), stroke(6.2))),
                group(items = listOf(polyline(listOf(60.0 to 16.0, 60.0 to 32.0, 76.0 to 32.0)), stroke(5.8))),
            ),
        )
        val lines = shapeLayer(
            index = 2,
            name = "TextLines",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(36.0 to 46.0, 64.0 to 46.0)),
                        polyline(listOf(36.0 to 60.0, 64.0 to 60.0)),
                        polyline(listOf(36.0 to 72.0, 54.0 to 72.0)),
                        trimPath(end = animScalar(listOf(Keyframe1D(0, 100.0), Keyframe1D(6, 0.0), Keyframe1D(40, 100.0), Keyframe1D(60, 100.0)))),
                        stroke(5.6),
                    )
                )
            ),
        )
        return buildComposition("tg_filelines", 60, listOf(sheet, lines))
    }

    private fun databaseJson(): String {
        val stack = shapeLayer(
            index = 1,
            name = "Database",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(22, 104.0, 96.0), Keyframe2D(42, 97.0, 105.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(ellipse(50.0, 27.0, 54.0, 20.0), duotoneFill(24.0), stroke(6.0))),
                group(items = listOf(polyline(listOf(23.0 to 27.0, 23.0 to 73.0)), polyline(listOf(77.0 to 27.0, 77.0 to 73.0)), stroke(6.0))),
                group(items = listOf(polyline(listOf(23.0 to 50.0, 50.0 to 58.0, 77.0 to 50.0)), polyline(listOf(23.0 to 73.0, 50.0 to 81.0, 77.0 to 73.0)), stroke(6.0))),
            ),
        )
        return buildComposition("tg_database", 60, listOf(stack))
    }

    private fun locationDotJson(): String {
        val pinBody = BezierPath(
            closed = true,
            vertices = listOf(50.0 to 15.0, 76.0 to 41.0, 50.0 to 80.0, 24.0 to 41.0),
            inTangents = listOf(-14.0 to 0.0, 0.0 to -14.0, 10.0 to -12.0, 0.0 to 14.0),
            outTangents = listOf(14.0 to 0.0, 0.0 to 14.0, -10.0 to -12.0, 0.0 to -14.0),
        )
        val pinLayer = shapeLayer(
            index = 1,
            name = "Pin",
            anchor = 50.0 to 80.0,
            position = animVec2(
                listOf(
                    Keyframe2D(0, 50.0, 80.0),
                    Keyframe2D(18, 50.0, 68.0, BezierEase.Overshoot),
                    Keyframe2D(38, 50.0, 82.0),
                    Keyframe2D(60, 50.0, 80.0),
                )
            ),
            shapes = listOf(
                group(items = listOf(path(pinBody), duotoneFill(24.0), stroke(6.2))),
                group(items = listOf(ellipse(50.0, 41.0, 15.0, 15.0), solidFill())),
            ),
        )
        val ripple = shapeLayer(
            index = 2,
            name = "Radar",
            anchor = 50.0 to 84.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 84.0), Keyframe2D(60, 50.0, 84.0))),
            scale = animScale(listOf(Keyframe2D(0, 45.0, 45.0), Keyframe2D(46, 120.0, 120.0), Keyframe2D(60, 45.0, 45.0))),
            opacity = animScalar(listOf(Keyframe1D(0, 75.0), Keyframe1D(46, 0.0), Keyframe1D(60, 0.0))),
            shapes = listOf(group(items = listOf(ellipse(50.0, 84.0, 42.0, 12.0), stroke(4.6)))),
        )
        return buildComposition("tg_location", 60, listOf(ripple, pinLayer))
    }

    private fun envelopeJson(): String {
        val env = shapeLayer(
            index = 1,
            name = "Envelope",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(20, 106.0, 106.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(rect(50.0, 52.0, 72.0, 50.0, 9.0), duotoneFill(20.0), stroke(6.2)))),
        )
        val flap = shapeLayer(
            index = 2,
            name = "Flap",
            anchor = 50.0 to 30.0,
            position = animVec2(listOf(Keyframe2D(0, 50.0, 30.0), Keyframe2D(60, 50.0, 30.0))),
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(24, 100.0, -45.0, BezierEase.Smooth), Keyframe2D(52, 100.0, 100.0), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(polyline(listOf(16.0 to 30.0, 50.0 to 56.0, 84.0 to 30.0)), stroke(6.0)))),
        )
        return buildComposition("tg_envelope", 60, listOf(env, flap))
    }

    private fun eyeJson(hidden: Boolean): String {
        val cleanEye = BezierPath(
            closed = true,
            vertices = listOf(14.0 to 50.0, 50.0 to 26.0, 86.0 to 50.0, 50.0 to 74.0),
            inTangents = listOf(0.0 to 0.0, -20.0 to 0.0, 0.0 to 0.0, 20.0 to 0.0),
            outTangents = listOf(10.0 to -14.0, 20.0 to 0.0, -10.0 to 14.0, -20.0 to 0.0),
        )
        val lids = shapeLayer(
            index = 1,
            name = "EyeLids",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(14, 100.0, 18.0), Keyframe2D(34, 100.0, 108.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(path(cleanEye), duotoneFill(20.0), stroke(6.2))),
                group(items = listOf(ellipse(50.0, 50.0, 22.0, 22.0), solidFill())),
            ),
        )
        if (!hidden) return buildComposition("tg_eye", 60, listOf(lids))
        val slash = shapeLayer(
            index = 2,
            name = "Slash",
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(20.0 to 20.0, 80.0 to 80.0)),
                        trimPath(end = animScalar(listOf(Keyframe1D(0, 100.0), Keyframe1D(6, 0.0), Keyframe1D(38, 100.0, BezierEase.Overshoot), Keyframe1D(60, 100.0)))),
                        stroke(6.8),
                    )
                )
            ),
        )
        return buildComposition("tg_eyeoff", 60, listOf(lids, slash))
    }

    private fun copyJson(): String {
        val back = shapeLayer(
            index = 1,
            name = "BackSheet",
            shapes = listOf(group(items = listOf(rect(39.0, 39.0, 44.0, 52.0, 8.0), duotoneFill(18.0), stroke(6.0)))),
        )
        val front = shapeLayer(
            index = 2,
            name = "FrontSheet",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(22, 57.0, 57.0, BezierEase.Overshoot), Keyframe2D(48, 50.0, 50.0), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(group(items = listOf(rect(61.0, 61.0, 44.0, 52.0, 8.0), duotoneFill(26.0), stroke(6.2)))),
        )
        return buildComposition("tg_copy", 60, listOf(back, front))
    }

    private fun pinJson(): String {
        val pin = shapeLayer(
            index = 1,
            name = "PushPin",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(18, 50.0, 40.0), Keyframe2D(34, 50.0, 54.0, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            rotation = animScalar(listOf(Keyframe1D(0, 0.0), Keyframe1D(20, 12.0), Keyframe1D(60, 0.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(34.0 to 20.0, 66.0 to 20.0, 60.0 to 46.0, 72.0 to 58.0, 28.0 to 58.0, 40.0 to 46.0), closed = true), duotoneFill(24.0), stroke(6.0))),
                group(items = listOf(polyline(listOf(50.0 to 58.0, 50.0 to 84.0)), stroke(6.4))),
            ),
        )
        return buildComposition("tg_pin", 60, listOf(pin))
    }

    private fun handWaveJson(): String {
        val hand = shapeLayer(
            index = 1,
            name = "WaveHand",
            anchor = 48.0 to 78.0,
            position = animVec2(listOf(Keyframe2D(0, 48.0, 78.0), Keyframe2D(60, 48.0, 78.0))),
            rotation = animScalar(
                listOf(
                    Keyframe1D(0, 0.0),
                    Keyframe1D(12, -16.0, BezierEase.Overshoot),
                    Keyframe1D(26, 18.0),
                    Keyframe1D(40, -12.0),
                    Keyframe1D(52, 8.0),
                    Keyframe1D(60, 0.0),
                )
            ),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(listOf(28.0 to 54.0, 24.0 to 32.0, 35.0 to 22.0, 44.0 to 40.0, 48.0 to 16.0, 59.0 to 18.0, 60.0 to 42.0, 72.0 to 26.0, 80.0 to 34.0, 70.0 to 72.0, 48.0 to 82.0, 30.0 to 70.0), closed = true),
                        duotoneFill(24.0),
                        stroke(6.0),
                    )
                )
            ),
        )
        return buildComposition("tg_wave", 60, listOf(hand))
    }

    private fun userPlusJson(): String {
        val user = shapeLayer(
            index = 1,
            name = "User",
            shapes = listOf(
                group(items = listOf(ellipse(42.0, 34.0, 24.0, 24.0), duotoneFill(24.0), stroke(6.2))),
                group(items = listOf(polyline(listOf(16.0 to 80.0, 22.0 to 61.0, 62.0 to 61.0, 68.0 to 80.0), closed = true), duotoneFill(22.0), stroke(6.2))),
            ),
        )
        val plus = shapeLayer(
            index = 2,
            name = "PlusBadge",
            anchor = 77.0 to 46.0,
            position = animVec2(listOf(Keyframe2D(0, 77.0, 46.0), Keyframe2D(60, 77.0, 46.0))),
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(22, 132.0, 132.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(77.0 to 35.0, 77.0 to 57.0)), polyline(listOf(66.0 to 46.0, 88.0 to 46.0)), stroke(6.4)))
            ),
        )
        return buildComposition("tg_userplus", 60, listOf(user, plus))
    }

    private fun swapJson(): String {
        val topArrow = shapeLayer(
            index = 1,
            name = "TopArrow",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(24, 58.0, 50.0, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(22.0 to 34.0, 78.0 to 34.0)), polyline(listOf(63.0 to 21.0, 78.0 to 34.0, 63.0 to 47.0)), stroke(6.4)))
            ),
        )
        val botArrow = shapeLayer(
            index = 2,
            name = "BotArrow",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(24, 42.0, 50.0, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(78.0 to 66.0, 22.0 to 66.0)), polyline(listOf(37.0 to 53.0, 22.0 to 66.0, 37.0 to 79.0)), stroke(6.4)))
            ),
        )
        return buildComposition("tg_swap", 60, listOf(topArrow, botArrow))
    }

    private fun warningJson(): String {
        val tri = shapeLayer(
            index = 1,
            name = "Triangle",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(20, 108.0, 108.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(50.0 to 16.0, 86.0 to 80.0, 14.0 to 80.0), closed = true), duotoneFill(24.0), stroke(6.4)))
            ),
        )
        val bang = shapeLayer(
            index = 2,
            name = "Bang",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(18, 50.0, 44.0, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(50.0 to 38.0, 50.0 to 56.0)), stroke(6.4))),
                group(items = listOf(ellipse(50.0, 68.0, 8.0, 8.0), solidFill())),
            ),
        )
        return buildComposition("tg_warning", 60, listOf(tri, bang))
    }

    private fun chevronJson(down: Boolean): String {
        val dx = if (down) 0.0 else 7.0
        val dy = if (down) 7.0 else 0.0
        val chevron = shapeLayer(
            index = 1,
            name = "Chevron",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(20, 50.0 + dx, 50.0 + dy, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(
                    items = listOf(
                        polyline(if (down) listOf(26.0 to 38.0, 50.0 to 62.0, 74.0 to 38.0) else listOf(38.0 to 24.0, 64.0 to 50.0, 38.0 to 76.0)),
                        stroke(7.2),
                    )
                )
            ),
        )
        return buildComposition("tg_chevron", 60, listOf(chevron))
    }

    private fun moreDotsJson(vertical: Boolean): String {
        fun dot(idx: Int, x: Double, y: Double, startT: Int): String = shapeLayer(
            index = idx,
            name = "Dot$idx",
            anchor = x to y,
            position = animVec2(listOf(Keyframe2D(0, x, y), Keyframe2D(60, x, y))),
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(startT, 100.0, 100.0),
                    Keyframe2D(startT + 10, 145.0, 145.0, BezierEase.Overshoot),
                    Keyframe2D(startT + 24, 100.0, 100.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(ellipse(x, y, 12.0, 12.0), solidFill()))),
        )
        val layers = if (vertical) {
            listOf(dot(1, 50.0, 26.0, 4), dot(2, 50.0, 50.0, 12), dot(3, 50.0, 74.0, 20))
        } else {
            listOf(dot(1, 26.0, 50.0, 4), dot(2, 50.0, 50.0, 12), dot(3, 74.0, 50.0, 20))
        }
        return buildComposition("tg_more", 60, layers)
    }

    private fun layerGroupJson(): String {
        fun diamond(idx: Int, y: Double, shift: Double, fillPct: Double): String = shapeLayer(
            index = idx,
            name = "Layer$idx",
            position = animVec2(listOf(Keyframe2D(0, 50.0, 50.0), Keyframe2D(22, 50.0, 50.0 + shift, BezierEase.Overshoot), Keyframe2D(60, 50.0, 50.0))),
            shapes = listOf(
                group(items = listOf(polyline(listOf(50.0 to y - 14.0, 82.0 to y, 50.0 to y + 14.0, 18.0 to y), closed = true), duotoneFill(fillPct), stroke(5.8)))
            ),
        )
        return buildComposition("tg_layers", 60, listOf(diamond(3, 66.0, 6.0, 14.0), diamond(2, 50.0, 0.0, 18.0), diamond(1, 34.0, -6.0, 24.0)))
    }

    private fun venusMarsJson(): String {
        val orb = shapeLayer(
            index = 1,
            name = "Gender",
            rotation = animScalar(listOf(Keyframe1D(0, 0.0), Keyframe1D(22, -12.0, BezierEase.Overshoot), Keyframe1D(60, 0.0))),
            shapes = listOf(
                group(items = listOf(ellipse(45.0, 45.0, 34.0, 34.0), duotoneFill(20.0), stroke(6.2))),
                group(items = listOf(polyline(listOf(57.0 to 33.0, 78.0 to 16.0)), polyline(listOf(63.0 to 16.0, 78.0 to 16.0, 78.0 to 31.0)), stroke(6.0))),
                group(items = listOf(polyline(listOf(45.0 to 62.0, 45.0 to 84.0)), polyline(listOf(35.0 to 74.0, 55.0 to 74.0)), stroke(6.0))),
            ),
        )
        return buildComposition("tg_gender", 60, listOf(orb))
    }

    private fun ringJson(): String {
        val core = shapeLayer(
            index = 1,
            name = "Core",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(24, 120.0, 120.0, BezierEase.Overshoot), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 60.0, 60.0), duotoneFill(20.0), stroke(6.5)))),
        )
        return buildComposition("tg_ring", 60, listOf(core))
    }

    private fun heartHandsJson(): String {
        val heart = BezierPath(
            closed = true,
            vertices = listOf(50.0 to 34.0, 82.0 to 36.0, 50.0 to 79.0, 18.0 to 36.0),
            inTangents = listOf(-12.0 to -16.0, 0.0 to -18.0, 22.0 to -16.0, 0.0 to 18.0),
            outTangents = listOf(12.0 to -16.0, 0.0 to 18.0, -22.0 to -16.0, 0.0 to -18.0),
        )
        val layer = shapeLayer(
            index = 1,
            name = "Heart",
            scale = animScale(
                listOf(
                    Keyframe2D(0, 100.0, 100.0),
                    Keyframe2D(16, 118.0, 118.0, BezierEase.Overshoot),
                    Keyframe2D(30, 96.0, 96.0),
                    Keyframe2D(44, 112.0, 112.0),
                    Keyframe2D(60, 100.0, 100.0),
                )
            ),
            shapes = listOf(group(items = listOf(path(heart), duotoneFill(26.0), stroke(6.2)))),
        )
        return buildComposition("tg_heart", 60, listOf(layer))
    }

    private fun genericPulseJson(): String {
        val ring = shapeLayer(
            index = 1,
            name = "GenericPulse",
            scale = animScale(listOf(Keyframe2D(0, 100.0, 100.0), Keyframe2D(28, 110.0, 110.0), Keyframe2D(60, 100.0, 100.0))),
            shapes = listOf(group(items = listOf(ellipse(50.0, 50.0, 62.0, 62.0), duotoneFill(20.0), stroke(6.2)))),
        )
        return buildComposition("tg_generic", 60, listOf(ring))
    }

    fun hasBespokeAnimation(iconName: String): Boolean = iconName in setOf(
        "Notification", "MessageBubble", "Refresh", "TrashCan", "PhoneSolid",
        "Sparkle", "UsersGroup", "UsersRectangle", "Download", "Search", "LightMode",
        "DarkMode", "AutoMode", "Filter", "Plus", "Send", "Droplet", "Check", "Book",
        "CalendarDays", "GraduationCap", "DoorOpen", "Crown", "Shield",
        "Palette", "Clock", "Edit", "Person", "SolidUser", "UserTie",
        "UserNurse", "Lock", "Login", "Logout", "InfoCircle", "HelpCircle",
        "Home", "Award", "Close", "BackArrow", "RightArrow",
        "Camera", "LanguageGlobe", "IdCard", "Registration", "FileLines",
        "Database", "LocationDot", "EnvelopeSolid", "Eye", "EyeOff",
        "Copy", "Pin", "HandWave", "UserPlus", "Swap", "Warning",
        "ChevronRight", "ChevronDown", "MoreVert", "MoreHoriz",
        "LayerGroup", "VenusMars", "Ring", "PrayingHands",
    )
}
