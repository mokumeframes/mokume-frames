// A separate headless design preview. Does not launch or control Minecraft or the desktop.
const {pathToFileURL}=require('node:url');
const path=require('node:path');
const fs=require('node:fs');
async function main(){
  const {chromium}=require(process.env.PLAYWRIGHT_PACKAGE||'playwright');
  const browser=await chromium.launch({headless:true});
  try{
    const page=await browser.newPage({viewport:{width:1600,height:900},deviceScaleFactor:1});
    await page.goto(pathToFileURL(path.join(__dirname,'../design/menu-preview.html')).href);
    for(const screen of ['title','loading','notebook']){
      await page.evaluate(v=>show(v),screen);
      await page.screenshot({path:path.join(__dirname,`../design/${screen}-preview.png`)});
    }
    console.log('Rendered three design previews in a hidden browser. These are not Minecraft screenshots.');
  }finally{await browser.close();}
}
main().catch(e=>{console.error(e);process.exitCode=1});
