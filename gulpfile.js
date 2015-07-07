var gulp        = require('gulp');
var sass        = require('gulp-sass');
var browserSync = require('browser-sync');
var sourcemaps = require('gulp-sourcemaps');

// Registering a 'less' task that just compile our LESS files to CSS


gulp.task('watch', ['sass'], function () {
  gulp.watch('./resources/sass/{,*/}*.{scss,sass}', ['sass'])
});


gulp.task('sass', function() {
  gulp.src('./resources/sass/main.scss')
      .pipe(sourcemaps.init())
      .pipe(sass({
        errLogToConsole: true
      }))
      .pipe(sourcemaps.write())
      .pipe(gulp.dest('./public/stylesheets'));
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
      'app/controllers/{,*/}*.scala',
      'conf/routes'],
    open: false
  });
});

// Creating the default gulp task
gulp.task('default', ['sass', 'watch', 'serve']);
