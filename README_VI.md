# Umamuse - Trò chơi Đua ngựa

## Tổng quan

Umamuse là một trò chơi di động Android về đua ngựa, kết hợp giữa lối chơi đua ngựa tương tác và cơ chế đặt cược. Ứng dụng tuân theo kiến trúc nhiều hoạt động (activities) với các dịch vụ và tiện ích được chia sẻ cho các chức năng như tùy chọn người dùng và nhạc nền.

## Các Activity

### 1. LoginActivity

- **Mục đích**: Điểm khởi đầu để xác thực người dùng
- **Chức năng chính**:
  - Xác thực đăng nhập người dùng
  - Điều hướng đến OpeningActivity sau khi đăng nhập thành công
  - Ghi nhớ thông tin đăng nhập
  - Xác thực tên người dùng và mật khẩu

### 2. OpeningActivity

- **Mục đích**: Phát video giới thiệu trước khi vào trò chơi chính
- **Chức năng chính**:
  - Phát video toàn màn hình với tỷ lệ phù hợp
  - Bắt buộc hiển thị ngang màn hình
  - Nút bỏ qua "Troll" di chuyển ngẫu nhiên khi nhấp vào
  - Tự động chuyển sang RaceActivity sau khi video kết thúc
  - Điều khiển và xử lý sự kiện phát video

### 3. RaceActivity

- **Mục đích**: Màn hình trò chơi chính nơi diễn ra các cuộc đua ngựa
- **Chức năng chính**:
  - Lựa chọn ngựa và chuẩn bị đua
  - Hoạt ảnh đua với chuyển động ngựa chân thực
  - Tích hợp hệ thống đặt cược
  - Nền đường đua cuộn mượt mà
  - Hiển thị và quản lý số dư
  - Điều hướng đến BetActivity và DepositMoneyActivity
  - Hộp thoại kết quả đua với tính toán tiền thắng
  - Đếm vòng và logic hoàn thành cuộc đua
  - Phát nhạc nền với tùy chọn tắt tiếng

### 4. BetActivity

- **Mục đích**: Giao diện đặt cược cho ngựa
- **Chức năng chính**:
  - Hiển thị các ngựa có sẵn bằng ViewPager2
  - Thiết lập số tiền đặt cược cho ngựa
  - Tính toán tỷ lệ cược và tiền thắng tiềm năng
  - Quản lý số dư người dùng cho việc đặt cược
  - Phát nhạc nền với tùy chọn tắt tiếng
  - Tổng hợp cược hiện tại và tiền thắng tiềm năng

### 5. DepositMoneyActivity

- **Mục đích**: Giao diện để nạp tiền vào số dư người dùng
- **Chức năng chính**:
  - Lựa chọn nhiều gói (Return Pack, Comeback Pack, God of Bet Pack)
  - Xác nhận mua gói
  - Cập nhật và hiển thị số dư
  - Phát nhạc nền với tùy chọn tắt tiếng
  - Điều hướng trở lại RaceActivity

### 6. MainActivity

- **Mục đích**: Có thể được sử dụng cho chức năng tương lai hoặc cài đặt ứng dụng ban đầu

## Các Model

### 1. Horse

- **Mục đích**: Đại diện cho một con ngựa trong trò chơi đua
- **Thuộc tính**:
  - id: Định danh duy nhất
  - name: Tên ngựa
  - imageRes: ID tài nguyên hình ảnh của ngựa
  - speed: Giá trị tốc độ hiện tại
  - isFinished: Trạng thái hoàn thành cuộc đua
  - isForward: Hướng di chuyển
  - lastDirectionChange: Thời điểm thay đổi hướng cuối cùng
  - directionInterval: Thời gian giữa các lần thay đổi hướng

### 2. HorseBet

- **Mục đích**: Đại diện cho cược đặt trên một con ngựa
- **Thuộc tính**:
  - horse: Tham chiếu đến đối tượng Horse
  - odds: Hệ số nhân cho tiền thắng tiềm năng
  - betAmount: Số tiền đặt cược cho con ngựa này
  - newBetAmount: Tiền cược mới bổ sung đang được đặt

## Repositories

### 1. HorseRepository

- **Mục đích**: Quản lý dữ liệu ngựa trên các activity
- **Chức năng**:
  - getAllHorses(): Trả về danh sách tất cả các ngựa có sẵn
  - getCurrentRaceHorses(): Lấy danh sách ngựa được chọn cho cuộc đua hiện tại
  - setCurrentRaceHorses(): Thiết lập ngựa cho cuộc đua hiện tại

## Adapters

### 1. HorseBetAdapter

- **Mục đích**: Kết nối dữ liệu đặt cược ngựa với ViewPager2 trong BetActivity
- **Chức năng**:
  - Hiển thị thông tin ngựa với hình ảnh
  - Xử lý nhập số tiền cược
  - Hiển thị tỷ lệ cược
  - Cập nhật đối tượng HorseBet với đầu vào của người dùng

## Services

### 1. BackgroundMusicService

- **Mục đích**: Quản lý phát nhạc nền trên tất cả các activity
- **Chức năng**:
  - Phát, tạm dừng và trộn các bài nhạc nền
  - Duy trì nhạc nhất quán giữa các chuyển đổi activity
  - Điều khiển mức âm lượng (mặc định 30%)
  - Xử lý chức năng tắt tiếng
  - Ghi nhớ tùy chọn tắt tiếng bằng SharedPreferences
  - Tự động phát bài tiếp theo khi một bài kết thúc

## Tiện ích

### 1. UserPreferences

- **Mục đích**: Quản lý lưu trữ và truy xuất dữ liệu người dùng
- **Chức năng**:
  - getUserBalance(): Lấy số dư hiện tại của người dùng
  - setUserBalance(): Đặt số dư người dùng thành giá trị cụ thể
  - addToUserBalance(): Thêm số tiền vào số dư hiện tại
  - subtractFromUserBalance(): Giảm số dư theo số tiền được chỉ định

### 2. MusicManager

- **Mục đích**: Hỗ trợ kết nối các activity với BackgroundMusicService
- **Chức năng**:
  - Liên kết/hủy liên kết activity với dịch vụ nhạc
  - Điều khiển phát nhạc và trạng thái tắt tiếng
  - Cập nhật trạng thái giao diện nút tắt tiếng
  - Xử lý vòng đời kết nối dịch vụ

## Các thành phần giao diện người dùng

### 1. Layout Activity

- **activity_race.xml**: Layout màn hình đua chính với các làn đường, nền và nút điều khiển
- **activity_bet.xml**: Giao diện đặt cược với ViewPager2 để chọn ngựa
- **deposit_money.xml**: Màn hình nạp tiền với các tùy chọn gói
- **activity_opening.xml**: Layout trình phát video cho đoạn mở đầu
- **dialog_race_result.xml**: Popup kết quả đua với tính toán tiền thắng

### 2. Drawables tùy chỉnh

- **btn_sound_selector.xml**: Trạng thái nút chuyển đổi tắt/bật tiếng
- **rounded_bg.xml**: Nền bo tròn cho các phần tử UI

## Cơ chế trò chơi

### 1. Hệ thống đua

- Ngựa di chuyển với tốc độ thay đổi và hoạt ảnh tự nhiên
- Thay đổi hướng tạo ra các mẫu chuyển động thực tế
- Đếm vòng với hiển thị vạch đích
- Lựa chọn người chiến thắng với nhấn mạnh trực quan

### 2. Hệ thống cá cược

- Tỷ lệ cược được tính cho từng con ngựa
- Hỗ trợ đặt cược nhiều lần trên các ngựa khác nhau
- Tích lũy cược trong một cuộc đua
- Tính toán tiền thắng dựa trên tỷ lệ và số tiền cược
- Tích hợp quản lý số dư

### 3. Kiếm tiền

- Hệ thống tiền tệ ảo với nhiều tùy chọn gói
- Lưu trữ số dư xuyên suốt các phiên
- Hiển thị rõ ràng số dư hiện tại

## Hệ thống âm thanh

- Nhạc nền với ba bài nhạc (bakushin.mp3, chiyo.mp3, mambo.mp3)
- Trộn bài để tăng tính đa dạng
- Điều khiển âm lượng ở mức 30% mặc định
- Nút chuyển đổi tắt tiếng với lưu trữ tùy chọn
- Phát nhạc liền mạch giữa các chuyển đổi activity

## Tính năng trải nghiệm người dùng

- Mở đầu video toàn màn hình
- Nút "troll" tương tác để tăng sự tham gia
- Định hướng ngang màn hình để xem đua tối ưu
- Các phần tử giao diện có hoạt ảnh để phản hồi tốt hơn
- Thông báo Toast cho các sự kiện quan trọng
- Âm thanh nhất quán trên toàn ứng dụng

## Thực hiện kỹ thuật

- Quản lý vòng đời Activity
- Liên kết dịch vụ cho các quy trình nền
- SharedPreferences để lưu trữ dữ liệu
- Hệ thống hoạt ảnh cho giao diện và phần tử trò chơi
- Tối ưu hóa phát đa phương tiện
- Quản lý tài nguyên cho âm thanh và hình ảnh
