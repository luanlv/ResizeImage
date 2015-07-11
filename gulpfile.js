
  var gulp        = require('gulp');
  var sass        = require('gulp-sass');
  var browserSync = require('browser-sync');
  var sourcemaps = require('gulp-sourcemaps');
  var minifyHTML = require('gulp-minify-html');

// Registering a 'less' task that just compile our LESS files to CSS


  gulp.task('watch', ['sass'], function () {
    gulp.watch('./resources/sass/{,*/}*.{scss,sass}', ['sass'])
  });

  gulp.task('watch2', ['sass'], function () {
    gulp.watch('./resources/html/*.{stream,html}', ['minify-html'])
  });

  gulp.task('minify-html', function() {
    var opts = {
      conditionals: true,
      spare:true,
      quotes: true,
      empty: true,
      cdata: true,
      loose: true
    };
    return gulp.src('./resources/html/*.{stream,html}')
        .pipe(minifyHTML(opts))
        .pipe(gulp.dest('./app/views'));
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
  gulp.task('default', ['sass', 'minify-html', 'watch', 'watch2', 'serve']);
