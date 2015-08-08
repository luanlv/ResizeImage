var gulp        = require('gulp');
var sass        = require('gulp-sass');

var gulpif = require('gulp-if');
var sprity = require('sprity');

var browserSync = require('browser-sync');
var sourcemaps = require('gulp-sourcemaps');
var minifyCss = require('gulp-minify-css');

// Registering a 'less' task that just compile our LESS files to CSS


gulp.task('sprites', function () {
  return sprity.src({
    src: './resources/icons/icons/*.{png,jpg}',
    style: './resources/sass/_sprite.scss',
    // ... other optional options
    // for example if you want to generate scss instead of css
    processor: 'sass', // make sure you have installed sprity-sass
  })
  .pipe(gulpif('*.png', gulp.dest('./public/icons/'), gulp.dest('./resources/sass/')))
});


gulp.task('sass', function() {
  gulp.src('./resources/sass/main.scss')
      .pipe(sourcemaps.init())
      .pipe(sass({
        errLogToConsole: true
      }))
      .pipe(sourcemaps.write())
      .pipe(minifyCss({compatibility: 'ie8'}))
      .pipe(gulp.dest('./public/stylesheets'));
});

gulp.task('watch', ['sass'], function () {
  gulp.watch('./resources/sass/{,*/}*.{scss,sass}', ['sass'])
});

//
gulp.task('serve', function () {
  browserSync({
    // By default, Play is listening on port 9000
    proxy: 'localhost:9000',
    // We will set BrowserSync on the port 9001
    port: 9001,
    // Reload all assets
    // Important: you need to specify the path on your source code
    // not the path on the url
    files: ['public/stylesheets/*.css',
      'public/javascripts/*.js',
      'app/views/*.html',
      'app/views/*.stream',
      'app/views/product/*.html',
        'app/controllers/{,*/}*.scala',
      'conf/routes'],
    open: false
  });
});

// Creating the default gulp task
gulp.task('default', [ 'sass', 'watch']);
