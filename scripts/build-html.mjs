import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';
import { minify } from 'html-minifier-terser';

const files = [
  ['src/main/html/api.html', 'src/main/resources/static/api.html'],
  ['src/main/html/api-example.html', 'src/main/resources/static/api-example.html']
];

const options = {
  collapseWhitespace: true,
  conservativeCollapse: true,
  minifyCSS: true,
  minifyJS: true,
  removeComments: true,
  removeRedundantAttributes: true,
  removeScriptTypeAttributes: true,
  removeStyleLinkTypeAttributes: true
};

for (const [source, target] of files) {
  const html = await readFile(source, 'utf8');
  const minified = await minify(html, options);
  const singleLine = minified.replace(/\r?\n/g, ' ').trim();

  await mkdir(dirname(target), { recursive: true });
  await writeFile(target, `${singleLine}\n`, 'utf8');
}
