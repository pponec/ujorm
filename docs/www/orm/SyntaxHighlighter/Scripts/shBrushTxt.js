/*
 * JsMin
 * Javascript Compressor
 * http://www.crockford.com/
 * http://www.smallsharptools.com/
*/

dp.sh.Brushes.TXT=function()
{var keywords='';var values='';var fonts='';this.regexList=[{regex:dp.sh.RegexLib.MultiLineCComments,css:'rem'}];this.CssClass='dp-css';this.Style='.dp-css .value { color: black; }'+'.dp-css .important { color: red; }';}
dp.sh.Highlighter.prototype.GetKeywordsCSS=function(str)
{return'\\b([a-z_]|)'+str.replace(/ /g,'(?=:)\\b|\\b([a-z_\\*]|\\*|)')+'(?=:)\\b';}
dp.sh.Highlighter.prototype.GetValuesCSS=function(str)
{return'\\b'+str.replace(/ /g,'(?!-)(?!:)\\b|\\b()')+'\:\\b';}
dp.sh.Brushes.TXT.prototype=new dp.sh.Highlighter();dp.sh.Brushes.TXT.Aliases=['txt'];
