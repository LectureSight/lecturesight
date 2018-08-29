Configuration Examples
======================

Logitech C920 Overview Camera, Axis V5915 PTZ Camera
----------------------------------------------------

::

    # --- Webcam V4L2 framesource
    cv.lecturesight.framesource.input.mrl=v4l2:///dev/logitech[width=640;height=360;Power Line Frequency=50 Hz;Focus, Auto=0;Focus (absolute)=0;Zoom, Absolute=200]
    cv.lecturesight.framesource.v4l.format=MJPEG
    cv.lecturesight.framesource.maxfps=25

    # --- VAPIX Configuration (Axis cameras)
    cv.lecturesight.vapix.camera.host=camera-hostname.some.domain
    cv.lecturesight.vapix.camera.username=root
    cv.lecturesight.vapix.camera.password=PASSWORD
    cv.lecturesight.vapix.camera.pan.min=-17000
    cv.lecturesight.vapix.camera.pan.max=17000
    cv.lecturesight.vapix.camera.tilt.min=-2000
    cv.lecturesight.vapix.camera.tilt.max=9000
    cv.lecturesight.vapix.camera.zoom.min=1
    cv.lecturesight.vapix.camera.zoom.max=9999
    cv.lecturesight.vapix.camera.zoom.maxspeed=1
    cv.lecturesight.vapix.updater.interval=100

    # --- Video Analysis
    cv.lecturesight.videoanalysis.change.threshold=30
    cv.lecturesight.videoanalysis.cell.activation.threshold=2
    cv.lecturesight.videoanalysis.object.cells.min=15
    cv.lecturesight.videoanalysis.object.cells.max=128
    cv.lecturesight.videoanalysis.object.dormant.max=1500
    cv.lecturesight.videoanalysis.object.match.threshold=15

    # --- Camera Steering Worker (relative movement)
    cv.lecturesight.ptz.steering.worker.relativemove.move.damp.pan=0.65
    cv.lecturesight.ptz.steering.worker.relativemove.move.damp.tilt=0.2
    cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.x=2000
    cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.y=2000
    cv.lecturesight.ptz.steering.worker.relativemove.move.stop.x=35
    cv.lecturesight.ptz.steering.worker.relativemove.move.stop.y=200
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left=-2500
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right=2500
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top=-600
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom=-5000

    # --- Pan-only Camera Operator
    cv.lecturesight.cameraoperator.ptz.pan=0.0
    cv.lecturesight.cameraoperator.ptz.tilt=0.4
    cv.lecturesight.cameraoperator.ptz.zoom=0.1
    cv.lecturesight.cameraoperator.ptz.frame.width=0.8
    cv.lecturesight.cameraoperator.ptz.timeout=500
    cv.lecturesight.cameraoperator.ptz.idle.preset=Home
    cv.lecturesight.cameraoperator.ptz.start.preset=Start
    cv.lecturesight.cameraoperator.ptz.target.limit=2

    # --- Scene Profile
    cv.lecturesight.profile.manager.active.profile=overview

    # --- Scheduler: watch the Galicaster schedule file
    cv.lecturesight.scheduler.enable=true
    cv.lecturesight.scheduler.schedule.file=/usr/share/galicaster-repository/attach/calendar.ical
    cv.lecturesight.scheduler.timezone.offset=2
    cv.lecturesight.scheduler.tracker.leadtime=10

    # --- Enable DropWizard metrics collection
    cv.lecturesight.util.metrics.enable=true

    # --- Save overview snapshots
    cv.lecturesight.framesource.snapshot.file=/opt/ls/record/overview.png

    # --- Status Reporting
    cv.lecturesight.status.enable=true
    cv.lecturesight.status.name=hahn1
    cv.lecturesight.status.url=http://lsdashboard.some.domain/lecturesight/
    cv.lecturesight.status.interval=60

Raspberry Pi Overview Camera, VISCA Vaddio HD-USB PTZ Camera
------------------------------------------------------------

::

    # --- Overview camera
    cv.lecturesight.framesource.input.mrl=rtph264://rpi-cam.some.domain:8554

    # --- VISCA Camera Configuration
    com.wulff.lecturesight.visca.port.device=/dev/ttyUSB0
    com.wulff.lecturesight.visca.updater.interval=200

    # --- Video Analysis
    cv.lecturesight.videoanalysis.change.threshold=48
    cv.lecturesight.videoanalysis.cell.activation.threshold=3
    cv.lecturesight.videoanalysis.object.cells.min=20
    cv.lecturesight.videoanalysis.object.cells.max=128
    cv.lecturesight.videoanalysis.object.dormant.max=1500

    # --- Camera Steering Worker
    cv.lecturesight.ptz.steering.worker.relativemove.move.damp.pan=0.5
    cv.lecturesight.ptz.steering.worker.relativemove.move.damp.tilt=0.1
    cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.x=3000
    cv.lecturesight.ptz.steering.worker.relativemove.move.alpha.y=1000
    cv.lecturesight.ptz.steering.worker.relativemove.move.stop.x=300
    cv.lecturesight.ptz.steering.worker.relativemove.move.stop.y=200
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.left=-5500
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.right=3870
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.top=-600
    cv.lecturesight.ptz.steering.worker.relativemove.scene.limit.bottom=-5000
    cv.lecturesight.ptz.steering.worker.relativemove.focus.fixed=true
    cv.lecturesight.ptz.steering.worker.relativemove.move.initial.delay=5000

    # --- PTZ Camera Operator
    cv.lecturesight.cameraoperator.ptz.pan=0.3
    cv.lecturesight.cameraoperator.ptz.tilt=-0.40
    cv.lecturesight.cameraoperator.ptz.zoom=0.55
    cv.lecturesight.cameraoperator.ptz.timeout=500
    cv.lecturesight.cameraoperator.ptz.idle.preset=0
    cv.lecturesight.cameraoperator.ptz.target.limit=2

    # -- Scene Profile
    cv.lecturesight.profile.manager.active.profile=overview
