package asia.aaii.dramamm

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.HapticFeedbackConstants
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast

/** DramaMM original native UI: creator-owned YouTube/TikTok playback, saved list and tap feedback. */
class MainActivity : Activity() {
    private val back = Color.rgb(22,7,24)
    private val panel = Color.rgb(49,18,53)
    private val accent = Color.rgb(254,53,118)
    private val white = Color.WHITE
    private val grey = Color.rgb(190,162,191)
    private val gold = Color.rgb(250,195,103)
    private val store by lazy { getSharedPreferences("dramamm",Context.MODE_PRIVATE) }
    private lateinit var frame: LinearLayout
    private lateinit var page: LinearLayout
    private var current = "Discover"
    private var choice = 0
    private var filter = ""
    data class Clip(val title:String,val source:String,val url:String,val caption:String,val genre:String)
    private val clips = listOf(
        Clip("Go Ahead — Episode 1","YouTube","https://www.youtube.com/watch?v=wzQbpp4SfN8","Myanmar captions claimed, unverified","Family"),
        Clip("Go Ahead — Episode 3","YouTube","https://www.youtube.com/watch?v=DX_bOJHlBvo","Myanmar captions claimed, unverified","Family"),
        Clip("Go Ahead — Episode 4","YouTube","https://www.youtube.com/watch?v=R6g5-btuGOk","Myanmar captions claimed, unverified","Family"),
        Clip("Secret Husband Was a Cold CEO","YouTube","https://www.youtube.com/watch?v=9hn-wRxRyIc","English captions claimed, unverified","CEO"),
        Clip("She Fled After One Night with a Ruthless CEO","YouTube","https://www.youtube.com/watch?v=lzuOmqBRu2c","English captions claimed, unverified","Romance"),
        Clip("Everyone Loves Me — Trailer","YouTube","https://www.youtube.com/watch?v=8HK2eyUTgVY","Caption status unverified","Trailer"),
        Clip("DramaBox Membership Teaser","YouTube","https://www.youtube.com/watch?v=pVfF5uncrvs","Caption status unverified","Teaser"),
        Clip("Troy Discovers the Truth","YouTube","https://www.youtube.com/watch?v=hymldAfAB7k","Caption status unverified","Romance"),
        Clip("မြန်မာစာတန်းထိုး ဟာသဇာတ်လမ်း","YouTube","https://www.youtube.com/watch?v=G3Y8TkSuas8","Myanmar captions claimed, unverified","Comedy"),
        Clip("DramaBox Creator Post","TikTok","https://www.tiktok.com/@dramaboxshorts/video/7507478113400491282","Caption status unverified","Shorts")
    )
    private val sources = listOf(
        "ReelShort · YouTube Shorts" to "https://www.youtube.com/@reelshortapp/shorts",
        "DramaBox · YouTube Shorts" to "https://www.youtube.com/@dramaboxapp/shorts",
        "NetShort · YouTube Shorts" to "https://www.youtube.com/@NetShort-app/shorts",
        "Huace TV · YouTube Shorts" to "https://www.youtube.com/@huacetvenglish/shorts",
        "ReelShort · TikTok" to "https://www.tiktok.com/@reelshortapp",
        "DramaBox · TikTok" to "https://www.tiktok.com/@dramaboxshorts",
        "NetShort · TikTok" to "https://www.tiktok.com/@netshortdramas"
    )
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=back
        window.navigationBarColor=back
        render("Discover")
    }
    private fun d(n:Int)=(resources.displayMetrics.density*n).toInt()
    private fun grad(a:Int,b:Int,r:Int=18)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(a,b)).apply { cornerRadius=d(r).toFloat() }
    private fun vbox()=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
    private fun hbox()=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
    private fun dims(w:Int=-1,h:Int=-2)=LinearLayout.LayoutParams(w,h)
    private fun label(text:String,size:Float=14f,color:Int=white,bold:Boolean=false)=TextView(this).apply {
        this.text=text; textSize=size; setTextColor(color)
        if(bold)setTypeface(typeface,Typeface.BOLD)
    }
    private fun line(text:String,size:Float=14f,color:Int=white,bold:Boolean=false,top:Int=12) {
        page.addView(label(text,size,color,bold),dims().apply { topMargin=d(top) })
    }
    private fun interactive(view:View, action:()->Unit) {
        view.isClickable=true
        view.isFocusable=true
        view.isSoundEffectsEnabled=false
        view.setOnTouchListener { target,event ->
            if(store.getBoolean("motion",true)) {
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> target.animate().scaleX(0.94f).scaleY(0.94f).setDuration(70L).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> target.animate().scaleX(1f).scaleY(1f).setDuration(125L).start()
                }
            }
            false
        }
        view.setOnClickListener { target ->
            if(store.getBoolean("sound",true)) {
                target.isSoundEffectsEnabled=true
                target.playSoundEffect(SoundEffectConstants.CLICK)
                target.isSoundEffectsEnabled=false
            }
            if(store.getBoolean("haptic",true))target.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            action()
        }
    }
    private fun tile(padding:Int=16)=vbox().apply {
        setPadding(d(padding),d(padding),d(padding),d(padding))
        background=grad(panel,Color.rgb(59,22,58),20)
    }
    private fun button(text:String,action:()->Unit,primary:Boolean=true)=label(text,14f,white,true).apply {
        gravity=Gravity.CENTER
        setPadding(d(16),d(13),d(16),d(13))
        background=if(primary)grad(accent,Color.rgb(164,26,90),14) else grad(Color.rgb(80,32,75),panel,14)
        interactive(this,action)
    }
    private fun action(text:String,fn:()->Unit,primary:Boolean=true,top:Int=12){
        page.addView(button(text,fn,primary),dims().apply { topMargin=d(top) })
    }
    private fun add(parent:LinearLayout,text:String,size:Float=14f,color:Int=white,bold:Boolean=false,space:Int=7){
        parent.addView(label(text,size,color,bold),dims().apply { topMargin=d(space) })
    }
    private fun title(text:String,subtitle:String=""){
        line(text,23f,white,true,17)
        if(subtitle.isNotBlank())line(subtitle,12f,grey,false,6)
    }
    private fun saved():Set<String> = store.getStringSet("saved",emptySet())?.toSet() ?: emptySet()
    private fun flipSaved(i:Int) {
        val set=saved().toMutableSet()
        if(!set.add(i.toString()))set.remove(i.toString())
        store.edit().putStringSet("saved",set).apply()
    }
    private fun render(destination:String) {
        current=destination
        frame=vbox().apply{setBackgroundColor(back)}
        setContentView(frame)
        val header=hbox().apply{setPadding(d(18),d(12),d(18),d(10))}
        header.addView(label("▶ DramaMM",23f,accent,true),dims(0,-2).apply{weight=1f})
        header.addView(button("⚙",{render("Settings")},false),dims(-2,-2))
        frame.addView(header,dims())
        val scroll=ScrollView(this).apply{isVerticalScrollBarEnabled=false;fillViewport=true}
        page=vbox().apply{setPadding(d(17),d(2),d(17),d(22))}
        scroll.addView(page)
        frame.addView(scroll,dims(-1,0).apply{weight=1f})
        when(destination){
            "Discover" -> discover()
            "Shorts" -> shorts()
            "Search" -> search()
            "My List" -> myList()
            "Profile" -> profile()
            "Settings" -> settings()
            "Sources" -> providers()
            "Details" -> details()
            "Player" -> player()
            "VIP" -> previewPage("♛ VIP Preview","A design preview only. No payment is active or required for creator videos.")
            "Rewards" -> previewPage("🎁 Rewards Preview","Coins and rewards are UI demonstrations; no cash-out or purchase system is active.")
            else -> discover()
        }
        if(destination!="Player"&&destination!="Details")bottom()
    }
    private fun bottom(){
        val row=hbox().apply{background=grad(Color.rgb(43,15,44),back,0);setPadding(d(7),d(10),d(7),d(10))}
        listOf("⌂" to "Discover","▶" to "Shorts","⌕" to "Search","♥" to "My List","☻" to "Profile").forEach{ pair ->
            val item=vbox().apply{gravity=Gravity.CENTER}
            item.addView(label(pair.first,22f,if(current==pair.second)accent else grey,true))
            item.addView(label(pair.second,10f,if(current==pair.second)accent else grey))
            interactive(item){render(pair.second)}
            row.addView(item,dims(0,-2).apply{weight=1f})
        }
        frame.addView(row)
    }
    private fun hero(){
        val box=tile(23).apply{minimumHeight=d(275);gravity=Gravity.BOTTOM;background=grad(Color.rgb(123,38,74),Color.rgb(33,12,58),25)}
        add(box,"★  DRAMAMM CINEMATIC",11f,gold,true,0)
        add(box,"Stories Worth Watching",30f,white,true,14)
        add(box,"Romance • Family • Comedy • Shorts",14f,grey,false,10)
        box.addView(button("▶ Watch now",{render("Shorts")}),dims().apply{topMargin=d(17)})
        page.addView(box,dims(-1,d(290)))
    }
    private fun card(i:Int,item:Clip){
        val box=tile()
        val top=hbox()
        val poster=label(if(item.source=="TikTok")"♪" else "▶",35f,white,true).apply{
            gravity=Gravity.CENTER
            background=grad(listOf(Color.rgb(142,37,92),Color.rgb(99,39,105),Color.rgb(112,61,55))[i%3],panel,15)
        }
        top.addView(poster,dims(d(95),d(115)))
        val info=vbox().apply{setPadding(d(13),0,0,0)}
        add(info,item.source.uppercase()+" • "+item.genre,12f,gold,true,0)
        add(info,item.title,17f,white,true,8)
        add(info,item.caption,11f,grey,false,9)
        top.addView(info,dims(0,-2).apply{weight=1f})
        box.addView(top)
        box.addView(button("▶ Details and player",{choice=i;render("Details")}),dims().apply{topMargin=d(12)})
        box.addView(button(if(saved().contains(i.toString()))"♥ Saved" else "♡ Save",{flipSaved(i);render(current)},false),dims().apply{topMargin=d(8)})
        page.addView(box,dims().apply{topMargin=d(12)})
    }
    private fun discover(){
        hero()
        title("Trending drama links","Sample videos require creator/subtitle verification")
        clips.take(5).forEachIndexed{i,item->card(i,item)}
        action("Browse all shorts",{render("Shorts")},false)
        title("Official channels")
        sources.take(3).forEach{pair->sourceCard(pair.first,pair.second)}
        action("All creators",{render("Sources")},false)
    }
    private fun shorts(){
        title("Shorts & Episodes","10 research candidates · not all are verified vertical Shorts")
        clips.forEachIndexed{i,item->card(i,item)}
        action("Official Shorts channels",{render("Sources")},false)
    }
    private fun search(){
        title("Search dramas","Search locally by title, genre and source")
        val field=EditText(this).apply{
            hint="Romance, CEO, Family..."
            setText(filter);textSize=16f
            setTextColor(white);setHintTextColor(grey)
            background=grad(panel,panel,12)
            setPadding(d(15),d(12),d(15),d(12))
            setSingleLine(true)
        }
        page.addView(field,dims().apply{topMargin=d(16)})
        action("Search",{filter=field.text.toString();render("Search")})
        listOf("Romance","CEO","Family","Comedy","").forEach{genre->
            action(if(genre.isEmpty())"Show all" else genre,{filter=genre;render("Search")},false,7)
        }
        clips.forEachIndexed{i,item->
            if(filter.isEmpty()||item.title.contains(filter,true)||item.genre.contains(filter,true)||item.source.contains(filter,true))card(i,item)
        }
    }
    private fun myList(){
        title("♥ My List","Saved on this device")
        var count=0
        clips.forEachIndexed{i,item->if(saved().contains(i.toString())){count++;card(i,item)}}
        if(count==0){line("No favorites yet.",15f,grey);action("Discover videos",{render("Shorts")})}
    }
    private fun profile(){
        title("DramaMM Profile","Guest user • local storage only")
        val info=tile()
        add(info,"◉ Guest Viewer",25f,white,true,0)
        add(info,"Saved "+saved().size+" • Sources "+sources.size+" • Review links "+clips.size,13f,grey)
        page.addView(info,dims().apply{topMargin=d(16)})
        action("⚙ Settings",{render("Settings")})
        action("♥ My List",{render("My List")},false)
        action("🎁 Rewards",{render("Rewards")},false)
        action("♛ VIP Concept",{render("VIP")},false)
        action("▶ Official video sources",{render("Sources")},false)
    }
    private fun settings(){
        title("⚙ Interaction Settings","Tap animation • click sound • haptic vibration")
        prefSwitch("Click Sound","sound","Native Android tap sound")
        prefSwitch("Haptic Vibration","haptic","Respects device settings")
        prefSwitch("Pop Animation","motion","Press-scale tap feedback")
        title("Subtitle preferences","Available captions are supplied by the publisher.")
        listOf("Auto","Myanmar","English").forEach{lang->
            action("Prefer "+lang,{store.edit().putString("language",lang).apply();Toast.makeText(this,"Preference: "+lang,Toast.LENGTH_SHORT).show()},false)
        }
        line("Current: "+store.getString("language","Auto"),12f,grey)
    }
    private fun prefSwitch(name:String,key:String,description:String){
        val row=hbox().apply{setPadding(0,d(10),0,d(8))}
        val labels=vbox()
        add(labels,name,17f,white,true,0)
        add(labels,description,12f,grey)
        row.addView(labels,dims(0,-2).apply{weight=1f})
        row.addView(Switch(this).apply{
            isChecked=store.getBoolean(key,true)
            setOnCheckedChangeListener { _, checked -> store.edit().putBoolean(key,checked).apply() }
        })
        page.addView(row,dims().apply{topMargin=d(10)})
    }
    private fun previewPage(heading:String,description:String){
        title(heading)
        line(description,16f,grey)
        action("Back to Profile",{render("Profile")},false)
    }
    private fun sourceCard(name:String,url:String){
        val box=tile()
        add(box,name,17f,white,true,0)
        add(box,"Original creator-managed source",12f,grey)
        box.addView(button("↗ Open official account",{open(url)}),dims().apply{topMargin=d(10)})
        page.addView(box,dims().apply{topMargin=d(12)})
    }
    private fun providers(){
        title("Creator Channels","Shorts collection • official external accounts")
        sources.forEach{sourceCard(it.first,it.second)}
    }
    private fun details(){
        val clip=clips[choice]
        action("‹ Back to shorts",{render("Shorts")},false)
        val box=tile(22).apply{background=grad(Color.rgb(123,40,78),Color.rgb(33,11,54),22)}
        add(box,"★ "+clip.genre.uppercase(),13f,gold,true,0)
        add(box,clip.title,27f,white,true,12)
        add(box,"Published on "+clip.source,13f,grey)
        page.addView(box,dims(-1,d(200)).apply{topMargin=d(12)})
        title("Video details")
        line(clip.caption,14f,grey)
        line("Research candidate: availability, subtitle and duration are not independently verified.",13f,grey)
        action("▶ Watch via provider embed",{render("Player")})
        action("↗ Open original "+clip.source,{open(clip.url)},false)
        action("♥ Toggle My List",{flipSaved(choice);render("Details")},false)
    }
    private fun player(){
        val clip=clips[choice]
        action("‹ Back",{render("Details")},false)
        title("Now Playing",clip.title)
        val yt=Regex("(?:v=|youtu\\.be/|shorts/)([A-Za-z0-9_-]{11})").find(clip.url)?.groupValues?.getOrNull(1)
        val tt=Regex("/video/(\\d+)").find(clip.url)?.groupValues?.getOrNull(1)
        val embed=if(yt!=null)"https://www.youtube.com/embed/"+yt+"?playsinline=1"
                  else if(tt!=null)"https://www.tiktok.com/embed/v2/"+tt
                  else ""
        if(embed.isNotEmpty()){
            val web=WebView(this).apply{
                settings.javaScriptEnabled=true
                settings.domStorageEnabled=true
                settings.mediaPlaybackRequiresUserGesture=true
                webChromeClient=WebChromeClient()
                webViewClient=WebViewClient()
                loadUrl(embed)
            }
            page.addView(web,dims(-1,d(440)).apply{topMargin=d(15)})
        }else line("This URL has no supported embed.",15f,grey)
        line("Embedded playback and captions depend on YouTube/TikTok policy and the original creator.",12f,grey)
        action("↗ Open official post",{open(clip.url)})
    }
    private fun open(url:String){
        try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}
        catch(_:Exception){Toast.makeText(this,"No compatible browser found",Toast.LENGTH_LONG).show()}
    }
}
