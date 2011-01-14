<map version="0.9.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node COLOR="#000000" CREATED="1294833131252" ID="ID_1772276602" MODIFIED="1294912274886" TEXT="&#x53cb;&#x6587;">
<font NAME="IPA X0208 P&#x30b4;&#x30b7;&#x30c3;&#x30af;" SIZE="20"/>
<hook NAME="accessories/plugins/AutomaticLayout.properties"/>
<node COLOR="#0033ff" CREATED="1294833149410" ID="ID_445460999" MODIFIED="1294881973633" POSITION="right" TEXT="TASKS">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<cloud COLOR="#c1fefe"/>
<font NAME="SansSerif" SIZE="18"/>
<node COLOR="#00b439" CREATED="1294833486247" ID="ID_1208900189" MODIFIED="1294902910375" TEXT="&#x53cb;&#x6587;&#x958b;&#x767a;">
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
<node COLOR="#990000" CREATED="1294834598050" ID="ID_1500755531" MODIFIED="1294981582306" TEXT="Stream&#x4e00;&#x89a7;">
<cloud COLOR="#fef2a5"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="idea"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="99"/>
<attribute NAME="task" VALUE="main_screen"/>
<attribute NAME="start" VALUE="2011-01-03"/>
<attribute NAME="complete" VALUE="50"/>
<attribute NAME="maxend" VALUE="2011-01-31"/>
<attribute NAME="note" VALUE="&quot;&#x30e1;&#x30a4;&#x30f3;&#x753b;&#x9762;&#x4f5c;&#x6210;&quot;"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<node COLOR="#111111" CREATED="1294981704478" ID="ID_1455395624" MODIFIED="1294981936023" TEXT="&#x57fa;&#x672c;&#x52d5;&#x4f5c;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="99"/>
<attribute NAME="task" VALUE="main_screen_base"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-03"/>
<attribute NAME="end" VALUE="2011-01-12"/>
</node>
<node COLOR="#111111" CREATED="1294835298400" FOLDED="true" ID="ID_364637509" MODIFIED="1294982497537" TEXT="Like&#x4ed8;&#x3051;&#x6a5f;&#x80fd;">
<icon BUILTIN="full-9"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="237"/>
<attribute NAME="task" VALUE="stream_like"/>
<attribute NAME="note" VALUE="&quot;&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3059;&#x3068;Like&#x304c;&#x5207;&#x308a;&#x66ff;&#x308f;&#x308b;&quot;"/>
<attribute NAME="depends" VALUE="!main_screen_base"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13-13:30"/>
<attribute NAME="end" VALUE="2011-01-14-12:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
<node COLOR="#111111" CREATED="1294901789114" ID="ID_1355953543" MODIFIED="1294980115562" TEXT="&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3057;&#x3066;View&#x306b;&#x53cd;&#x5fdc;&#x3092;&#x8fd4;&#x3059;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="159"/>
<attribute NAME="task" VALUE="stream_like_signal_to_view"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13-13:30"/>
<attribute NAME="end" VALUE="2011-01-13-15:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
<node COLOR="#111111" CREATED="1294902250795" ID="ID_1894472013" MODIFIED="1294980120682" TEXT="Like&#x306e;&#x30ea;&#x30af;&#x30a8;&#x30b9;&#x30c8;&#x3092;&#x3059;&#x308b;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="303"/>
<attribute NAME="task" VALUE="stream_like_request_to_server"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="depends" VALUE="main_screen.stream_like.stream_like_signal_to_view"/>
<attribute NAME="start" VALUE="2011-01-13-15:00"/>
<attribute NAME="end" VALUE="2011-01-13-18:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
<node COLOR="#111111" CREATED="1294902341890" ID="ID_1905663529" MODIFIED="1294980124610" TEXT="Like&#x30ea;&#x30af;&#x30a8;&#x30b9;&#x30c8;&#x306e;&#x623b;&#x308a;&#x5024;&#x5224;&#x5b9a;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="321"/>
<attribute NAME="task" VALUE="stream_like_parse_response_from_server"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="depends" VALUE="main_screen.stream_like.stream_like_request_to_server"/>
<attribute NAME="start" VALUE="2011-01-14-09:00"/>
<attribute NAME="end" VALUE="2011-01-14-10:30"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
<node COLOR="#111111" CREATED="1294902475244" ID="ID_692072539" MODIFIED="1294980131273" TEXT="&#x753b;&#x9762;&#x306b;&#x53cd;&#x6620;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="375"/>
<attribute NAME="task" VALUE="stream_like_update_view"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="depends" VALUE="main_screen.stream_like.stream_like_parse_response_from_server"/>
<attribute NAME="start" VALUE="2011-01-14-10:30"/>
<attribute NAME="end" VALUE="2011-01-14-12:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
</node>
<node COLOR="#111111" CREATED="1294835307729" FOLDED="true" ID="ID_1606489186" MODIFIED="1294981877326" TEXT="&#x30b3;&#x30e1;&#x30f3;&#x30c8;&#x4ed8;&#x3051;&#x6a5f;&#x80fd;">
<icon BUILTIN="full-9"/>
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="267"/>
<attribute NAME="task" VALUE="stream_comment"/>
<attribute NAME="note" VALUE="&quot;&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3059;&#x3068;Stream&#x9078;&#x629e;&#x5f8c;&#x753b;&#x9762;&#x306b;&#x5207;&#x308a;&#x66ff;&#x308f;&#x308b;&quot;"/>
<attribute NAME="depends" VALUE="!main_screen_base"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13-13:30"/>
<attribute NAME="end" VALUE="2011-01-14-14:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
<node COLOR="#111111" CREATED="1294901813327" ID="ID_1990863322" MODIFIED="1294966767513" TEXT="&#x30dc;&#x30bf;&#x30f3;&#x3092;&#x62bc;&#x3057;&#x3066;View&#x306b;&#x53cd;&#x5fdc;&#x3092;&#x8fd4;&#x3059;">
<icon BUILTIN="button_ok"/>
<attribute_layout VALUE_WIDTH="227"/>
<attribute NAME="task" VALUE="stream_comment_signal_to_view"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13-13:30"/>
<attribute NAME="end" VALUE="2011-01-13-15:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
<node COLOR="#111111" CREATED="1294974205074" ID="ID_1273931295" MODIFIED="1294981252834" TEXT="Stream&#x9078;&#x629e;&#x5f8c;&#x753b;&#x9762;&#x3092;&#x958b;&#x304f;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="267"/>
<attribute NAME="task" VALUE="stream_comment_pressed_open_stream_item_view"/>
<attribute NAME="depends" VALUE="!stream_comment_signal_to_view"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-14-13:30"/>
<attribute NAME="end" VALUE="2011-01-14-14:00"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
</node>
<node COLOR="#111111" CREATED="1294834858079" ID="ID_341747899" MODIFIED="1294997077392" TEXT="&#x753b;&#x50cf;&#x8868;&#x793a;">
<icon BUILTIN="full-4"/>
<icon BUILTIN="ksmiletris"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="303"/>
<attribute NAME="task" VALUE="image_display"/>
<attribute NAME="depends" VALUE="!stream_comment"/>
<attribute NAME="complete" VALUE="1"/>
<attribute NAME="note" VALUE="&quot;&#x753b;&#x50cf;&#x304c;&#x8ffd;&#x52a0;&#x3055;&#x308c;&#x3066;&#x3044;&#x308b;&#x30c7;&#x30fc;&#x30bf;&#x306e;&#x5834;&#x5408;&#x3001;&#x753b;&#x50cf;&#x3092;&#x8868;&#x793a;&#x3059;&#x308b;&quot;"/>
<attribute NAME="start" VALUE="2011-01-14-15:30"/>
</node>
<node COLOR="#111111" CREATED="1294835507943" ID="ID_1666749371" MODIFIED="1294984703444" TEXT="&#x30ea;&#x30f3;&#x30af;&#x81ea;&#x52d5;&#x4ed8;&#x52a0;">
<icon BUILTIN="full-3"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="141"/>
<attribute NAME="task" VALUE="stream_message_add_link"/>
<attribute NAME="depends" VALUE="!image_display"/>
<attribute NAME="complete" VALUE="0"/>
</node>
<node COLOR="#111111" CREATED="1294905043416" ID="ID_1224831516" MODIFIED="1294905278657" TEXT="&#x8d77;&#x52d5;&#x6642;&#x30a2;&#x30a4;&#x30c6;&#x30e0;&#x30af;&#x30ea;&#x30a2;">
<icon BUILTIN="full-2"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="165"/>
<attribute NAME="task" VALUE="stream_data_clear_when_boot"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<attribute NAME="complete" VALUE="0"/>
<attribute NAME="depends" VALUE="!stream_comment"/>
</node>
<node COLOR="#111111" CREATED="1294980084834" FOLDED="true" ID="ID_258271573" MODIFIED="1294997081492" TEXT="&#x4e0d;&#x5177;&#x5408;">
<icon BUILTIN="closed"/>
<attribute_layout NAME_WIDTH="45" VALUE_WIDTH="105"/>
<attribute NAME="task" VALUE="bugs"/>
<attribute NAME="depends" VALUE="!main_screen_base"/>
<node COLOR="#111111" CREATED="1294879224933" HGAP="23" ID="ID_1798956205" MODIFIED="1294980108210" TEXT="&#x30a2;&#x30a4;&#x30b3;&#x30f3;&#x304c;&#x8868;&#x793a;&#x3055;&#x308c;&#x306a;&#x3044;&#x4e0d;&#x5177;&#x5408;&#x306e;&#x4fee;&#x6b63;" VSHIFT="-1">
<edge STYLE="sharp_linear"/>
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="63" VALUE_WIDTH="267"/>
<attribute NAME="task" VALUE="main_screen_icon_bug_1"/>
<attribute NAME="start" VALUE="2011-01-12"/>
<attribute NAME="end" VALUE="2011-01-13"/>
<attribute NAME="note" VALUE="&quot;&#x753b;&#x50cf;&#x53d6;&#x5f97;&#x30d0;&#x30c3;&#x30d5;&#x30a1;&#x304c;&#x3064;&#x307e;&#x3063;&#x3066;&#x3044;&#x308b;&#x6a21;&#x69d8;&quot;"/>
<attribute NAME="statusnote" VALUE="&quot;&#x753b;&#x50cf;&#x53d6;&#x5f97;&#x5f85;&#x3061;&#x884c;&#x5217;&#x304c;&#x30af;&#x30ea;&#x30a2;&#x3055;&#x308c;&#x3066;&#x3044;&#x306a;&#x3044;&#x3063;&#x307d;&#x3044;&quot;"/>
<attribute NAME="flags" VALUE="task_completed"/>
<attribute NAME="complete" VALUE="100"/>
</node>
<node COLOR="#111111" CREATED="1294888040817" HGAP="30" ID="ID_1569210992" MODIFIED="1294966343077" TEXT="Attach&#x3055;&#x308c;&#x305f;&#x753b;&#x50cf;&#x304c;&#x8868;&#x793a;&#x3055;&#x308c;&#x306a;&#x3044;" VSHIFT="-1">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="231"/>
<attribute NAME="task" VALUE="main_screen_not_visible_attached_image"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="start" VALUE="2011-01-13-09:00"/>
<attribute NAME="end" VALUE="2011-01-13-12:30"/>
<attribute NAME="flags" VALUE="task_completed"/>
</node>
<node COLOR="#111111" CREATED="1294980181648" FOLDED="true" ID="ID_750103841" MODIFIED="1294996959742" TEXT="Like&#x30d7;&#x30ed;&#x30b0;&#x30ec;&#x30b9;&#x8868;&#x793a;&#x4e2d;&#x306e;&#x30b9;&#x30af;&#x30ed;&#x30fc;&#x30eb;">
<icon BUILTIN="button_ok"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="243"/>
<attribute NAME="task" VALUE="main_screen_bug_like_progress_scroll_out"/>
<attribute NAME="complete" VALUE="100"/>
<attribute NAME="priority" VALUE="500"/>
<attribute NAME="depends" VALUE="!main_screen_not_visible_attached_image"/>
<attribute NAME="start" VALUE="2011-01-14-14:00"/>
<attribute NAME="end" VALUE="2011-01-14-15:30"/>
<attribute NAME="flags" VALUE="task_completed"/>
<node COLOR="#111111" CREATED="1294980535638" ID="ID_1706715941" MODIFIED="1294980654151">
<richcontent TYPE="NODE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Like&#25276;&#19979;&#24460;&#12289;&#12503;&#12525;&#12464;&#12524;&#12473;&#34920;&#31034;&#20013;&#12395;&#30011;&#38754;&#12434;&#12473;&#12463;&#12525;&#12540;&#12523;&#12373;&#12379;&#30011;&#38754;&#22806;&#12395;&#20986;&#12375;&#12383;&#24460;&#12289;&#20877;&#12403;&#30011;&#38754;&#12395;&#20837;&#12428;&#12427;&#12392;&#12503;&#12525;&#12464;&#12524;&#12473;&#34920;&#31034;&#12398;&#12414;&#12414;&#27490;&#12414;&#12425;&#12394;&#12356;
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
</node>
</node>
<node COLOR="#990000" CREATED="1294835142870" ID="ID_540942687" MODIFIED="1294982484593" TEXT="Stream&#x9078;&#x629e;&#x5f8c;&#x753b;&#x9762;">
<cloud COLOR="#dffe7b"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="idea"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="111"/>
<attribute NAME="task" VALUE="stream_item_screen"/>
<attribute NAME="start" VALUE="2011-01-14-14:00"/>
<attribute NAME="allocate" VALUE="yakumo"/>
<attribute NAME="complete" VALUE="0"/>
<node COLOR="#111111" CREATED="1294982335941" ID="ID_1100166137" MODIFIED="1294982446817" TEXT="&#x30ec;&#x30a4;&#x30a2;&#x30a6;&#x30c8;&#x4f5c;&#x6210;">
<icon BUILTIN="full-9"/>
<attribute_layout NAME_WIDTH="51" VALUE_WIDTH="183"/>
<attribute NAME="task" VALUE="stream_item_screen_make_layout"/>
<attribute NAME="complete" VALUE="0"/>
<attribute NAME="start" VALUE="2011-01-14-14:00"/>
</node>
</node>
<node COLOR="#990000" CREATED="1294835121964" ID="ID_1463653292" MODIFIED="1294974234073" TEXT="&#x6295;&#x7a3f;&#x753b;&#x9762;">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1294835184219" ID="ID_1225705478" MODIFIED="1294881973641" TEXT="&#x901a;&#x5e38;&#x6295;&#x7a3f;"/>
<node COLOR="#111111" CREATED="1294835211046" ID="ID_1880077270" MODIFIED="1294881973641" TEXT="&#x753b;&#x50cf;&#x6295;&#x7a3f;">
<node COLOR="#111111" CREATED="1294835258107" ID="ID_1228838529" MODIFIED="1294881973641" TEXT="&#x30ab;&#x30e1;&#x30e9;&#x64ae;&#x5f71;"/>
<node COLOR="#111111" CREATED="1294835271677" ID="ID_291256516" MODIFIED="1294881973641" TEXT="&#x30e9;&#x30a4;&#x30d6;&#x30e9;&#x30ea;&#x9078;&#x629e;"/>
</node>
<node COLOR="#111111" CREATED="1294835225336" ID="ID_455867064" MODIFIED="1294881973641" TEXT="&#x30ea;&#x30f3;&#x30af;&#x6295;&#x7a3f;"/>
<node COLOR="#111111" CREATED="1294835234393" ID="ID_1579489143" MODIFIED="1294881973641" TEXT="&#x4f4d;&#x7f6e;&#x6295;&#x7a3f;"/>
</node>
</node>
</node>
<node COLOR="#0033ff" CREATED="1294833159403" ID="ID_1072881584" MODIFIED="1294881973645" POSITION="right" TEXT="RESOURCES">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<cloud COLOR="#b8d4b8"/>
<font NAME="SansSerif" SIZE="18"/>
<node COLOR="#00b439" CREATED="1294833178941" ID="ID_1101158092" MODIFIED="1294966473537" TEXT="Kazuya Yakumo">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="ksmiletris"/>
<attribute NAME="resource" VALUE="yakumo"/>
<attribute NAME="workinghours" VALUE="sat, sun off"/>
<attribute NAME="workinghours" VALUE="mon - fri 9:00 - 12:30, 13:30 - 18:00"/>
</node>
</node>
<node COLOR="#0033ff" CREATED="1294833168860" ID="ID_1588326060" MODIFIED="1294881973647" POSITION="right" TEXT="ACCOUNTS">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<font NAME="SansSerif" SIZE="18"/>
</node>
</node>
</map>
