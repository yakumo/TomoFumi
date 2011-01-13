<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node COLOR="#000000" CREATED="1294833131252" ID="ID_1772276602" MODIFIED="1294898465931" TEXT="&#x53cb;&#x6587;">
<font NAME="IPA X0208 P&#x30b4;&#x30b7;&#x30c3;&#x30af;" SIZE="20"/>
<hook NAME="accessories/plugins/AutomaticLayout.properties"/>
<node COLOR="#0033ff" CREATED="1294833149410" ID="ID_445460999" MODIFIED="1294881973633" POSITION="right" TEXT="TASKS">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<cloud COLOR="#c1fefe"/>
<font NAME="SansSerif" SIZE="18"/>
<node COLOR="#00b439" CREATED="1294833486247" ID="ID_1208900189" MODIFIED="1294888915326" TEXT="&#x53cb;&#x6587;&#x958b;&#x767a;">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="gohome"/>
<attribute NAME="task" VALUE="tomofumi"/>
<attribute NAME="start" VALUE="2011-01-03"/>
<attribute NAME="milestone" VALUE=""/>
</node>
<node COLOR="#00b439" CREATED="1294879599775" ID="ID_1253271811" MODIFIED="1294881973636" TEXT="&#x958b;&#x767a;&#x30bf;&#x30b9;&#x30af;">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<node COLOR="#990000" CREATED="1294834598050" ID="ID_1500755531" MODIFIED="1294897913438" TEXT="Stream&#x4e00;&#x89a7;">
<cloud COLOR="#fef2a5"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="idea"/>
<attribute NAME="task" VALUE="main_screen"/>
<attribute NAME="start" VALUE="2011-01-03"/>
<attribute NAME="complete" VALUE="30"/>
<attribute NAME="maxend" VALUE="2011-01-17"/>
<attribute NAME="note" VALUE="&quot;&#x30e1;&#x30a4;&#x30f3;&#x753b;&#x9762;&#x4f5c;&#x6210;&quot;"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<node COLOR="#111111" CREATED="1294879224933" HGAP="23" ID="ID_1798956205" MODIFIED="1294889441799" TEXT="&#x30a2;&#x30a4;&#x30b3;&#x30f3;&#x304c;&#x8868;&#x793a;&#x3055;&#x308c;&#x306a;&#x3044;&#x4e0d;&#x5177;&#x5408;&#x306e;&#x4fee;&#x6b63;" VSHIFT="-1">
<edge STYLE="sharp_linear"/>
<icon BUILTIN="button_ok"/>
<attribute NAME="task" VALUE="main_screen_icon_bug_1"/>
<attribute NAME="start" VALUE="2011-01-12"/>
<attribute NAME="end" VALUE="2011-01-13"/>
<attribute NAME="note" VALUE="&quot;&#x753b;&#x50cf;&#x53d6;&#x5f97;&#x30d0;&#x30c3;&#x30d5;&#x30a1;&#x304c;&#x3064;&#x307e;&#x3063;&#x3066;&#x3044;&#x308b;&#x6a21;&#x69d8;&quot;"/>
<attribute NAME="statusnote" VALUE="&quot;&#x753b;&#x50cf;&#x53d6;&#x5f97;&#x5f85;&#x3061;&#x884c;&#x5217;&#x304c;&#x30af;&#x30ea;&#x30a2;&#x3055;&#x308c;&#x3066;&#x3044;&#x306a;&#x3044;&#x3063;&#x307d;&#x3044;&quot;"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<attribute NAME="flags" VALUE="task_completed"/>
<attribute NAME="complete" VALUE="100"/>
</node>
<node COLOR="#111111" CREATED="1294888040817" HGAP="30" ID="ID_1569210992" MODIFIED="1294898231920" TEXT="Attach&#x3055;&#x308c;&#x305f;&#x753b;&#x50cf;&#x304c;&#x8868;&#x793a;&#x3055;&#x308c;&#x306a;&#x3044;" VSHIFT="-1">
<icon BUILTIN="button_ok"/>
<attribute NAME="task" VALUE="main_screen_not_visible_attached_image"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13"/>
<attribute NAME="end" VALUE="2011-01-13"/>
</node>
<node COLOR="#111111" CREATED="1294835298400" ID="ID_364637509" MODIFIED="1294898461098" TEXT="Like&#x4ed8;&#x3051;&#x6a5f;&#x80fd;">
<icon BUILTIN="full-9"/>
<attribute NAME="task" VALUE="stream_like"/>
<attribute NAME="note" VALUE="&quot;&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3059;&#x3068;Like&#x304c;&#x5207;&#x308a;&#x66ff;&#x308f;&#x308b;&quot;"/>
<attribute NAME="depends" VALUE="!main_screen_not_visible_attached_image"/>
<attribute NAME="complete" VALUE="0"/>
</node>
<node COLOR="#111111" CREATED="1294835307729" ID="ID_1606489186" MODIFIED="1294889596680" TEXT="&#x30b3;&#x30e1;&#x30f3;&#x30c8;&#x4ed8;&#x3051;&#x6a5f;&#x80fd;">
<icon BUILTIN="full-9"/>
<attribute NAME="task" VALUE="stream_comment"/>
<attribute NAME="note" VALUE="&quot;&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3059;&#x3068;Stream&#x9078;&#x629e;&#x5f8c;&#x753b;&#x9762;&#x306b;&#x5207;&#x308a;&#x66ff;&#x308f;&#x308b;&quot;"/>
<attribute NAME="depends" VALUE="!stream_like"/>
<attribute NAME="complete" VALUE="0"/>
</node>
<node COLOR="#111111" CREATED="1294834858079" ID="ID_341747899" MODIFIED="1294889602263" TEXT="&#x753b;&#x50cf;&#x8868;&#x793a;">
<icon BUILTIN="full-4"/>
<attribute_layout VALUE_WIDTH="134"/>
<attribute NAME="task" VALUE="image_display"/>
<attribute NAME="depends" VALUE="!stream_comment"/>
<attribute NAME="complete" VALUE="0"/>
</node>
<node COLOR="#111111" CREATED="1294835507943" ID="ID_1666749371" MODIFIED="1294889607463" TEXT="&#x30ea;&#x30f3;&#x30af;&#x81ea;&#x52d5;&#x4ed8;&#x52a0;">
<icon BUILTIN="full-3"/>
<attribute NAME="task" VALUE="stream_message_add_link"/>
<attribute NAME="depends" VALUE="!image_display"/>
<attribute NAME="complete" VALUE="0"/>
</node>
</node>
<node COLOR="#990000" CREATED="1294835121964" FOLDED="true" ID="ID_1463653292" MODIFIED="1294888371615" TEXT="&#x6295;&#x7a3f;&#x753b;&#x9762;">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1294835184219" ID="ID_1225705478" MODIFIED="1294881973641" TEXT="&#x901a;&#x5e38;&#x6295;&#x7a3f;"/>
<node COLOR="#111111" CREATED="1294835211046" ID="ID_1880077270" MODIFIED="1294881973641" TEXT="&#x753b;&#x50cf;&#x6295;&#x7a3f;">
<node COLOR="#111111" CREATED="1294835258107" ID="ID_1228838529" MODIFIED="1294881973641" TEXT="&#x30ab;&#x30e1;&#x30e9;&#x64ae;&#x5f71;"/>
<node COLOR="#111111" CREATED="1294835271677" ID="ID_291256516" MODIFIED="1294881973641" TEXT="&#x30e9;&#x30a4;&#x30d6;&#x30e9;&#x30ea;&#x9078;&#x629e;"/>
</node>
<node COLOR="#111111" CREATED="1294835225336" ID="ID_455867064" MODIFIED="1294881973641" TEXT="&#x30ea;&#x30f3;&#x30af;&#x6295;&#x7a3f;"/>
<node COLOR="#111111" CREATED="1294835234393" ID="ID_1579489143" MODIFIED="1294881973641" TEXT="&#x4f4d;&#x7f6e;&#x6295;&#x7a3f;"/>
</node>
<node COLOR="#990000" CREATED="1294835142870" ID="ID_540942687" MODIFIED="1294881973641" TEXT="Stream&#x9078;&#x629e;&#x5f8c;&#x753b;&#x9762;">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
</node>
<node COLOR="#0033ff" CREATED="1294833159403" ID="ID_1072881584" MODIFIED="1294881973645" POSITION="right" TEXT="RESOURCES">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<cloud COLOR="#b8d4b8"/>
<font NAME="SansSerif" SIZE="18"/>
<node COLOR="#00b439" CREATED="1294833178941" ID="ID_1101158092" MODIFIED="1294882205267" TEXT="Kazuya Yakumo">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="ksmiletris"/>
<attribute NAME="resource" VALUE="yakumo"/>
</node>
</node>
<node COLOR="#0033ff" CREATED="1294833168860" ID="ID_1588326060" MODIFIED="1294881973647" POSITION="right" TEXT="ACCOUNTS">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<font NAME="SansSerif" SIZE="18"/>
</node>
</node>
</map>