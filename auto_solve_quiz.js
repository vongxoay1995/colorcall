(async function() {
    // === DỮ LIỆU ĐÃ GHI NHỚ (30 CÂU TỪ BỘ ĐỀ TRƯỚC) ===
    const memory = [
        { "question": "Theo Báo cáo thường niên Tập đoàn Hòa Phát 2025 (công bố ngày 15/04/2026), hãy điền từ còn thiếu vào mô tả sau để hoàn thành phần giới thiệu về các lĩnh vực hoạt động của Tập đoàn Hòa Phát", "correctAnswer": "Điện máy gia dụng" },
        { "question": "Đối với cán bộ nhân viên thường xuyên làm việc/trao đổi thông qua email với bên ngoài, thói quen nào sau đây là giải pháp cơ bản để phòng ngừa rủi ro lừa đảo?", "correctAnswer": "Luôn cảnh giác với các yếu tố hối thúc; kiểm tra kỹ địa chỉ người gửi và xác thực chéo trước khi thực hiện các yêu cầu tài chính hoặc chia sẻ dữ liệu" },
        { "question": "Giải chạy \"30 ngày tôi khỏe\" 2024 của Tập đoàn Hòa Phát gắn với chương trình CSR (Trách nhiệm xã hội của doanh nghiệp) nào sau đây?", "correctAnswer": "Hành trình của nước" },
        { "question": "Theo Báo cáo thường niên năm 2025, tỷ trọng đóng góp của nhóm thép (bao gồm Gang thép và Sản phẩm thép) trong lợi nhuận của Tập đoàn 2025 là bao nhiêu %?", "correctAnswer": "83%" },
        { "question": "Đến tháng 5/2026, có bao nhiêu tài khoản Kỹ thuật viên được ghi nhận trên app Điện Máy Hòa Phát?", "correctAnswer": "Hơn 19.000 tài khoản kỹ thuật viên" },
        { "question": "Điền cụm từ còn thiếu trong thông điệp về Tầm nhìn của Hòa Phát: “Trở thành Tập đoàn sản xuất [...] với chất lượng dẫn đầu, trong đó Thép là lĩnh vực cốt lõi”.", "correctAnswer": "Công nghiệp" },
        { "question": "Điền từ còn thiếu vào chỗ trống để hoàn thành thông điệp xuất hiện trên trang bìa của \"Báo cáo thường niên 2025\": \"Doanh nghiệp […] - Vững vàng […]\"", "correctAnswer": "Kiến tạo - Tăng trưởng" },
        { "question": "Hội đồng Quản trị Tập đoàn Hòa Phát nhiệm kỳ 2026-2031 gồm bao nhiêu thành viên?", "correctAnswer": "10 thành viên" },
        { "question": "Đoạn audio sau giới thiệu về sản phẩm/nội dung thuộc Tổng Công ty nào?", "correctAnswer": "Tổng Công ty Nông nghiệp" },
        { "question": "Theo Báo cáo thường niên Tập đoàn Hòa Phát 2025 (công bố ngày 15/04/2026), trong nội dung Mô hình hoạt động, những Công ty nào sau đây trực thuộc Tổng Công ty Gang thép:", "correctAnswer": "Công ty TNHH Thép Hòa Phát Hưng Yên; Công ty CP Thép Hòa Phát Hải Dương; Công ty CP Thép Hòa Phát Dung Quất; Công ty CP Đầu tư Khoáng sản An Thông; Công ty CP Vận tải Biển Hòa Phát" },
        { "question": "Ghép nối tên các Dự án Khu công nghiệp (KCN) với định hướng chuyên biệt của từng Khu công nghiệp:", "correctAnswer": "KCN số 6 - Tập trung thu hút các ngành công nghệ cao; KCN Hoàng Diệu - Phát triển theo mô hình khu công nghiệp tổng hợp, đa ngành; KCN số 2 (tên trước đây là KCN Lý Thường Kiệt) - Định hướng phát triển công nghiệp hỗ trợ; KCN Đồng Phúc - Khu công nghiệp tổng hợp & đa ngành, ưu tiên các ngành công nghiệp xanh" },
        { "question": "Điền số thích hợp vào chỗ trống để hoàn thành nội dung sau: \"Năm Hòa Phát hoàn thành quá trình tái cấu trúc mô hình hoạt động của Tập đoàn theo 05 Tổng Công ty phụ trách từng lĩnh vực: Gang thép - Sản phẩm thép - Nông nghiệp - Bất động sản - Điện máy Gia dụng\".", "correctAnswer": "2021" },
        { "question": "Ca khúc trong MV chủ đề của Lễ Vinh danh Khen thưởng - Hoà Phát Awards lần thứ I là ca khúc nào?", "correctAnswer": "Từ bàn tay này" },
        { "question": "Trong năm 2025, sự kiện nội bộ nào sau đây thu hút gần 8.700 CBCNV tham gia thi trực tuyến?", "correctAnswer": "Cuộc thi “Tôi là Hòa Phát - Hòa Phát là tôi”" },
        { "question": "Theo quy định về màu sắc an toàn (TCVN 8092:2021), biển báo hình tròn với nền màu xanh dương (xanh da trời) biểu thị điều gì?", "correctAnswer": "Biển báo Chỉ thị bắt buộc (yêu cầu phải thực hiện hành động cụ thể như đeo kính, đội mũ bảo hộ, ...)" },
        { "question": "Theo thống kê từ các đề cử hạng mục giải thưởng Đổi mới sáng tạo - Hòa Phát Awards lần thứ I, toàn Tập đoàn Hòa Phát có bao nhiêu sáng kiến được triển khai và tổng giá trị làm lợi mang lại trong năm 2025 là bao nhiêu tỷ đồng?", "correctAnswer": "313 sáng kiến - Mang lại giá trị làm lợi gần 1.272 tỷ đồng" },
        { "question": "Những nhận định nào sau đây là ĐÚNG?\n(Có thể chọn một hoặc nhiều đáp án)", "correctAnswer": "Mã chứng khoán của Nông nghiệp Hoà Phát trên sàn HOSE là HPA; Công ty CP Phát triển Nông nghiệp Hoà Phát được thành lập năm 2016" },
        { "question": "Chiếc cúp trong ảnh dưới đây được tạo hình từ chữ “Nhân”, mang ý nghĩa đặt con người vào vị trí trung tâm của mọi thành tựu mà Tập đoàn tạo ra. Chiếc cúp này gắn với sự kiện nào?", "correctAnswer": "Lễ Vinh danh Khen thưởng Hoà Phát Awards lần thứ I" },
        { "question": "Máy làm mát không khí hoạt động theo nguyên lý nào?", "correctAnswer": "Làm mát bằng hơi nước tự nhiên" },
        { "question": "Chọn đáp án đúng nhất để điền vào chỗ trống trong câu sau: \"Bên cạnh vật phẩm thiết thực, điểm đặc biệt xuất hiện trong phần quà Tết Bính Ngọ 2026 là [...] của Chủ tịch HĐQT Trần Đình Long\".", "correctAnswer": "Thư chúc Tết" },
        { "question": "Điền từ còn thiếu vào chỗ trống để hoàn thành nội dung mô tả sau: Website Nội bộ của Tập đoàn Hòa Phát có [số] Chuyên mục.", "correctAnswer": "Bốn" },
        { "question": "Điền số thích hợp vào chỗ trống để hoàn thành nội dung sau: \"Hòa Phát vinh dự đón nhận Huân chương Lao động hạng Nhì của Chủ tịch nước vào năm đánh dấu [...] năm hình thành và phát triển của Tập đoàn\".", "correctAnswer": "30" },
        { "question": "Đâu là những yếu tố giúp dòng sản phẩm ống thép chất lượng cao của Hòa Phát là lựa chọn số 1 của các Doanh nghiệp FDI? (Có thể chọn một hoặc nhiều đáp án)", "correctAnswer": "Chủng loại đa dạng; Đáp ứng được yêu cầu cao về chất lượng và tiêu chuẩn quốc tế; Năng lực cung cấp vượt trội" },
        { "question": "Điện máy gia dụng Hòa Phát bắt đầu đưa vào vận hành dây chuyền sản xuất bếp từ và máy hút mùi khi nào?", "correctAnswer": "Năm 2024" },
        { "question": "Chu trình PDCA – mô hình cốt lõi của tiêu chuẩn Hệ thống Quản lý Chất lượng ISO 9001:2015 mang ý nghĩa nào dưới đây:", "correctAnswer": "P-Plan (Lập kế hoạch); D-Do (Thực hiện); C-Check (Kiểm tra); A-Act (Cải tiến)" },
        { "question": "Máy lọc nước là sản phẩm chủ lực của Công ty nào sau đây?", "correctAnswer": "Công ty CP Điện máy gia dụng Hòa Phát Hà Nam" },
        { "question": "Ghép nối thông tin về bối cảnh công việc và trang phục phù hợp tại nơi làm việc:", "correctAnswer": "Người lao động làm việc tại vị trí sản xuất trực tiếp - Mặc bảo hộ lao động do Công ty cấp phát; Bộ phận Lễ tân, Bảo vệ, Kỹ thuật ban quản lý tòa nhà - Mặc đồng phục theo quy định; Người lao động làm việc tại văn phòng - Mặc trang phục công sở sạch sẽ, gọn bắt gọn hoặc đồng phục theo quy định" },
        { "question": "Nhận định nào sau đây là SAI?", "correctAnswer": "Năm 2015 Hòa Phát niêm yết Cổ phiếu Nông nghiệp Hoà Phát HPA trên thị trường chứng khoán Việt Nam" },
        { "question": "Hệ thống trang trại tại Phú Thọ của Công ty TNHH MTV Gia Cầm Hòa Phát Phú Thọ có tổng diện tích là bao nhiêu?", "correctAnswer": "Trên 50 ha" },
        { "question": "\"Lò cao số 4 - Khu liên hợp gang thép Hòa Phát Dung Quất được đưa vào hoạt động, đánh dấu việc hoàn thành toàn bộ dự án (Giai đoạn 01), nâng tổng công suất thép thô của Hòa Phát lên 8,5 triệu tấn/năm, lớn nhất khu vực Đông Nam Á\" từ thời gian nào?", "correctAnswer": "Tháng 01/2021" }
    ];

    // === HÀM TIỆN ÍCH MÔ PHỎNG CON NGƯỜI ===
    const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));
    const randomDelay = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;

    console.log(`%c[Auto-Bot] Đã nạp ${memory.length} câu hỏi. Đang bắt đầu làm bài...`, 'color: #00ff00; font-size: 16px; font-weight: bold;');

    const textElements = document.querySelectorAll('label, div, span, p, td'); 
    let solved = 0;

    for (let item of memory) {
        // Cắt một đoạn ngắn của câu hỏi để nhận diện trên trang web mới
        const qSnippet = item.question.substring(0, 35).replace(/\s+/g, ' ').trim();
        
        // Cắt đáp án (Xử lý cả câu hỏi cho phép tích chọn nhiều đáp án phân tách bằng dấu chấm phẩy)
        const aTexts = item.correctAnswer.split(';').map(s => s.replace(/\s+/g, ' ').trim()).filter(s => s);
        
        const pageContent = document.body.innerText.replace(/\s+/g, ' ');

        if (pageContent.includes(qSnippet)) {
            for (let aText of aTexts) {
                let targetClick = null;
                
                // Quét DOM tìm đáp án
                for (let el of textElements) {
                    const elText = (el.innerText || el.textContent || '').replace(/\s+/g, ' ').trim();
                    if (elText === aText || (elText.includes(aText) && elText.length < aText.length + 15)) {
                        // Ưu tiên các thẻ label chứa input
                        if (el.tagName === 'LABEL') { targetClick = el; break; } 
                        else if (el.closest('label')) { targetClick = el.closest('label'); break; } 
                        else if (el.querySelector('input')) { targetClick = el; break; } 
                        else { targetClick = el; }
                    }
                }

                if (targetClick && !targetClick.hasAttribute('data-auto-clicked')) {
                    targetClick.setAttribute('data-auto-clicked', 'true');
                    
                    // 1. NGẪU NHIÊN: Mô phỏng thời gian đọc và suy nghĩ (2s đến 4.5s)
                    const readTime = randomDelay(2000, 4500);
                    console.log(`Đang đọc câu: "${qSnippet}..." (chờ ${readTime}ms)`);
                    await sleep(readTime);

                    // Cuộn trang mượt mà
                    targetClick.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    
                    // 2. NGẪU NHIÊN: Mô phỏng thời gian di chuyển chuột (0.6s đến 1.8s)
                    const moveTime = randomDelay(600, 1800);
                    await sleep(moveTime);

                    // Click chốt đáp án
                    console.log(`%c => Tự động click: "${aText}"`, 'color: #00ffff');
                    targetClick.click();
                    
                    const innerInput = targetClick.querySelector('input');
                    if (innerInput && !innerInput.checked) { innerInput.click(); }
                    
                    // 3. Nghỉ ngơi trước khi sang câu/đáp án khác (0.3s - 1s)
                    await sleep(randomDelay(300, 1000));
                }
            }
            solved++;
        }
    }

    console.log(`%c[Auto-Bot] Hoàn tất! Đã tự động điền ${solved} câu có trên trang web.`, 'color: #00ff00; font-size: 16px; font-weight: bold;');
    if (solved < memory.length) {
         console.log(`%c(Nếu bài thi có nhiều trang, hãy tự bấm sang trang tiếp theo và chạy lại script này!)`, 'color: yellow;');
    }
})();
